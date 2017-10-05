package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
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
//        StorageMessages.RequestMsg requestMsg = builder.build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setRequestMsg(builder)
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
        if(checkSum.equals(cache.getFirstCheckSum())){
            System.out.println("retrieve success :)");
        }
        else{
            System.out.println("retrieve failed :(");
            System.out.println("retrieved file's checksum is: " + checkSum);
            System.out.println("original file's checksum is: " + cache.getFirstCheckSum());
        }

    }

    public void retrieveOneChunkAndStore(String filename, String chunkIdHost) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String[] info = chunkIdHost.split(" ");
        int chunkId = Integer.parseInt(info[0]);
        Socket socket1 = new Socket(info[1], Integer.parseInt(info[2]));
        StorageMessages.StorageMessageWrapper returnMsgWrapper = sendMsgToDataNode(filename, chunkIdHost, chunkId, socket1);
        if(returnMsgWrapper != null){
            if(returnMsgWrapper.getDataMsg().getSuccess().equals("success")){
                storeChunkOnDisk(filename, chunkId, returnMsgWrapper);
            }
            else{
                System.out.println("The file has been corrupted on the datanode side");
                sendFixInfoToServer(filename, chunkId, info[1], info[2]);
                if(info.length > 3){
                    Socket socket2 = new Socket(info[3], Integer.parseInt(info[4]));
                    returnMsgWrapper = sendMsgToDataNode(filename, chunkIdHost, chunkId, socket2);
                    if(returnMsgWrapper != null){
                        if(returnMsgWrapper.getDataMsg().getSuccess().equals("success")){
                            storeChunkOnDisk(filename, chunkId, returnMsgWrapper);
                        }
                        else{
                            System.out.println("The file has been corrupted on the datanode side");
                            sendFixInfoToServer(filename, chunkId, info[3], info[4]);
                            if(info.length > 5){
                                Socket socket3 = new Socket(info[5], Integer.parseInt(info[6]));
                                returnMsgWrapper = sendMsgToDataNode(filename, chunkIdHost, chunkId, socket3);
                                if(returnMsgWrapper != null){
                                    if(returnMsgWrapper.getDataMsg().getSuccess().equals("success")){
                                        storeChunkOnDisk(filename, chunkId, returnMsgWrapper);
                                    }
                                    else{
                                        sendFixInfoToServer(filename, chunkId, info[5], info[6]);
                                        // send something to the server that this chunk has been complete invalid
                                    }
                                }
                                else{
                                    System.out.println("return message is empty");
                                }
                                socket3.close();
                            }
                            else{
                                // send something to the server that this chunk has been complete invalid
                            }
                        }
                    }
                    else{
                        System.out.println("return message is empty");
                    }
                    socket2.close();
                }
                else{
                    // send something to the server that this chunk has been complete invalid
                }
            }
            socket1.close();
        }
    }


    private void sendFixInfoToServer(String filename, int chunkId, String hostname, String port) throws IOException {
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        String host = hostname + " " + port;
        StorageMessages.FixInfoMsg fixInfoMsg
                = StorageMessages.FixInfoMsg.newBuilder()
                .setFilename(filename)
                .setChunkId(chunkId)
                .setHost(host).build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder().setFixInfoMsg(fixInfoMsg).build();
        msgWrapper.writeTo(toServerSocket.getOutputStream());
    }


    private void storeChunkOnDisk(String filename, int chunkId, StorageMessages.StorageMessageWrapper returnMsgWrapper) throws NoSuchAlgorithmException, IOException {
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
    }

    private StorageMessages.StorageMessageWrapper sendMsgToDataNode(String filename, String chunkIdHost, int chunkId, Socket socket) throws IOException, InterruptedException {
        System.out.println("Chunk Info: " + chunkIdHost);
        StorageMessages.DataMsg dataMsg
                = StorageMessages.DataMsg.newBuilder()
                .setChunkId(chunkId)
                .setFilename(filename)
                .setType("retrieve").build();
        StorageMessages.StorageMessageWrapper dataMsgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setDataMsg(dataMsg)
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
        }
        else{
            System.out.println("get something from the storage node...");
        }
        return returnMsgWrapper;
    }


}
