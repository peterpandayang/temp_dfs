package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import com.sun.org.apache.bcel.internal.generic.StoreInstruction;
import edu.usfca.cs.cache.ClientCache;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.thread.ClientRetrieveFileThread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientFileRetriever {

    private String filename;
    private String myHostname;
    private FileIO io = new FileIO();
    private ThreadPoolExecutor threadPool;
    private ClientCache cache;

    public ClientFileRetriever(String filename, String myHostname, ThreadPoolExecutor threadPool){
        this.filename = filename;
        this.myHostname = myHostname;
        this.threadPool = threadPool;
        cache = new ClientCache();
    }

    public void startGetReq() throws IOException, InterruptedException, NoSuchAlgorithmException {
        sendGetReq();
    }

    public void sendGetReq() throws IOException, InterruptedException, NoSuchAlgorithmException {
        // this is the main logic
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        String myHost = myHostname + " " + toServerSocket.getLocalPort();
        StorageMessages.RequestMsg.Builder builder
                = StorageMessages.RequestMsg.newBuilder()
                .setFilename(filename)
                .setType("get");
        builder.addHostBytes(ByteString.copyFromUtf8(myHost));
        StorageMessages.RequestMsg requestMsg = builder.build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setRequestMsg(requestMsg)
                        .build();
        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        // get response from the server
        StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(toServerSocket.getInputStream());
        int attempt = 0;
        while(returnMsgWrapper == null && attempt <= 999){
            attempt++;
            returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(toServerSocket.getInputStream());
            Thread.sleep(10);
        }

        StorageMessages.RequestMsg returnRequestMsg = returnMsgWrapper.getRequestMsg();
        List<String> chunkIdHostList = returnRequestMsg.getChunkIdHostList();
        for(String chunkIdHost : chunkIdHostList){
            ClientRetrieveFileThread thread = new ClientRetrieveFileThread(this, filename, chunkIdHost);
            threadPool.execute(thread);
        }
        // if the data returned is empty, should ask the server again
        toServerSocket.close();

        // here should check for the running thread and do the merge of the files
        while(threadPool.getActiveCount() != 0){
            Thread.sleep(10);
        }
        System.out.println("the running threads are all finished.");
        mergeFiles(filename);
    }

    private void mergeFiles(String filename) throws IOException, NoSuchAlgorithmException {
        String copyFromPath = cache.RETRIEVE_TEMP_PATH + "/files/" + filename;
        String copyToPath = cache.RETRIEVE_PATH + "/files/";
        if(!Files.exists(Paths.get(copyToPath))){
            Files.createDirectories(Paths.get(copyToPath));
        }
        File file = new File(copyToPath + "/" + filename);
        Path filePath = Paths.get(file.getPath());
        if(!Files.exists(filePath)){
            Files.createFile(filePath);
        }
        else{
            Files.delete(filePath);
            Files.createFile(filePath);
        }
        DirectoryStream<Path> fileList = Files.newDirectoryStream(Paths.get(copyFromPath));

        Iterator<Path> iterator = fileList.iterator();
        List<Path> list = new ArrayList<>();
        while(iterator.hasNext()){
            list.add(iterator.next());
        }
        Collections.sort(list, new Comparator<Path>() {
            public int compare( Path a, Path b ) {
                String[] astr = a.toString().split("/");
                String[] bstr = b.toString().split("/");
                int ia = Integer.parseInt(astr[astr.length - 1]);
                int ib = Integer.parseInt(bstr[bstr.length - 1]);
                return ia - ib;
            }
        } );

        for(Path path : list) {
            String oneFile = path.toString();
            System.out.println("file is : " + oneFile);
            String content = io.getFileContent(new File(oneFile));
            Files.write(filePath, content.getBytes(), StandardOpenOption.APPEND);
        }
        String dataString = new String(Files.readAllBytes(Paths.get(file.getPath())));
        String checkSum = io.getCheckSum(dataString);
//        if(checkSum.equals(cache.getFirstCheckSum())){
        if(cache.checkInCheckSumMap(filename, checkSum)){
            System.out.println("retrieve success :)");
        }
        else{
            System.out.println("retrieve failed :(");
            System.out.println("retrieved file's checksum is: " + checkSum);
            System.out.println("original file's checksum is: " + cache.getFileCheckSum(filename));
        }

    }

    public void retrieveOneChunkAndStore(String filename, String chunkIdHost) throws IOException, InterruptedException, NoSuchAlgorithmException {
        System.out.println("The original chunkIdHost is : " + chunkIdHost);
        String[] allChunkIdHosts = chunkIdHost.split(" ");
        List<String> list = new ArrayList<>();
        String chunkId = allChunkIdHosts[0];
        for(int i = 1; i <= allChunkIdHosts.length - 2; i += 2){
            if(allChunkIdHosts[i].trim().length() != 0){
                String s = chunkId + " " + allChunkIdHosts[i] + " " + allChunkIdHosts[i + 1];
                System.out.println("add " + s + " to the list");
                list.add(s);
            }
        }
        boolean hasRetrieved = false;
        int count = 0;
        while(!hasRetrieved){
            if(count != 0){
                System.out.println("send get data again to another datanode : " + list.get(count));
            }
            hasRetrieved = sendgetReqHelper(filename, list.get(count));
            if(hasRetrieved){
                break;
            }
            // should send something to the controller
            Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
            List<String> temp = new ArrayList<>();
            temp.add(list.get(count));
            StorageMessages.RequestMsg requestMsg =
                    StorageMessages.RequestMsg.newBuilder()
                    .setType("fix")
                    .setFilename(filename)
                    .addAllChunkIdHost(temp).build();
            StorageMessages.StorageMessageWrapper msgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                    .setRequestMsg(requestMsg).build();
            msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
            toServerSocket.close();
            count++;
            System.out.println("This file has corrupted");
        }

    }

    private boolean sendgetReqHelper(String filename, String chunkIdHost) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String[] info = chunkIdHost.split(" ");
        int chunkId = Integer.parseInt(info[0]);
        Socket socket = new Socket(info[1], Integer.parseInt(info[2]));
        System.out.println("Chunk Info: " + chunkIdHost);
        StorageMessages.DataMsg.Builder dataMsgBuilder
                = StorageMessages.DataMsg.newBuilder()
                .setChunkId(chunkId)
                .setFilename(filename)
                .setType("retrieve");
        StorageMessages.StorageMessageWrapper dataMsgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setDataMsg(dataMsgBuilder.build())
                        .build();
        dataMsgWrapper.writeDelimitedTo(socket.getOutputStream());
        // here should get the info...
        StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
        int attempt = 0;
        while(returnMsgWrapper == null && attempt <= 999){
            attempt++;
            returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            Thread.sleep(10);
        }
        if(returnMsgWrapper == null){
            System.out.println("nothing from the datanode...");
            return false;
        }
        else{
            System.out.println("get something from the storage node...");
        }
        if(returnMsgWrapper != null && !returnMsgWrapper.getDataMsg().getSuccess().equals("false")){
            StorageMessages.DataMsg dataMsg = returnMsgWrapper.getDataMsg();
            String folderPath = cache.RETRIEVE_TEMP_PATH + "/files/" + filename;
            String checksum = dataMsg.getChecksum();
            String data = dataMsg.getData().toStringUtf8();
            String checkSheckSum = io.getCheckSum(data);
            if(checkSheckSum.equals(checksum)){
                System.out.println("network working well and client side checksum matched...");
            }
            else{
                System.out.println("network has some problem and data is lost...");
            }
            if(!Files.exists(Paths.get(folderPath))){
                Files.createDirectories(Paths.get(folderPath));
            }
            System.out.println("write to chunkId : " + chunkId);
            File file = new File(folderPath + "/" + chunkId);
            io.writeGeneralFile(file, dataMsg.getData().toStringUtf8());
            socket.close();
            return true;
        }
        return false;
    }

}
