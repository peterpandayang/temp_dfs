package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.thread.RequestedFixThread;
import edu.usfca.cs.thread.RequestingFixThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeFixRouter {

    private Socket socket;  // this is socket from server to datanode;
    private StorageMessages.StorageMessageWrapper msgWrapper;
    private String myHost;
    private FileIO io = new FileIO();
    private DataNodeCache cache;

    public DataNodeFixRouter(Socket socket, StorageMessages.StorageMessageWrapper msgWrapper, String myHost, DataNodeCache cache){
        this.socket = socket;
        this.msgWrapper = msgWrapper;
        this.myHost = myHost;
        this.cache = cache;
    }

    public void startReqFixThread(){
        RequestingFixThread thread = new RequestingFixThread(this);
        thread.start();
    }

    /**
     * asking other datanode to provide the replicas
     */
    public void startRequsting() throws IOException, InterruptedException, NoSuchAlgorithmException {
        StorageMessages.FixInfoMsg fixInfoMsg = msgWrapper.getFixInfoMsg();
        String host = fixInfoMsg.getHost();
        String filename = fixInfoMsg.getFilename();
        int chunkId = fixInfoMsg.getChunkId();
        String[] hostInfo = host.split(" ");
        StorageMessages.FixDataMsg fixDataMsg
                = StorageMessages.FixDataMsg.newBuilder()
                .setFilename(filename)
                .setChunkId(chunkId).build();
        StorageMessages.StorageMessageWrapper msgWrapper
                = StorageMessages.StorageMessageWrapper.newBuilder()
                .setFixDataMsg(fixDataMsg)
                .build();
        Socket socket = new Socket(hostInfo[0], Integer.parseInt(hostInfo[1]));
        msgWrapper.writeTo(socket.getOutputStream());
        // here should get the info...
        StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
        int attempt = 0;
        while(returnMsgWrapper == null && attempt <= 999){
            attempt++;
            returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            Thread.sleep(10);
        }
        if(returnMsgWrapper != null){
            if(returnMsgWrapper.getFixDataMsg().getSuccess().equals("success")){
                StorageMessages.FixDataMsg fixDataMsg1 = returnMsgWrapper.getFixDataMsg();
                String port = myHost.split(" ")[1];
                String folderPath = DataNodeCache.PATH + "/" + port + "/files/" + filename;
                if(!Files.exists(Paths.get(folderPath))){
                    Files.createDirectories(Paths.get(folderPath));
                }
                String data = fixDataMsg1.getData().toStringUtf8();
                String checksum = fixDataMsg1.getChecksum();
                String checkCheckSum = io.getCheckSum(data);
                if(checkCheckSum.equals(checksum)){
                    System.out.println("network works well");
                    File file = new File(folderPath + "/" + chunkId);
                    io.writeGeneralFile(file, data);
                    File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
                    io.writeGeneralFile(checkSum, checksum);
                    if(io.fileIsValid(file, checkSum)){
                        cache.updateFileInfo(filename, chunkId);
                        System.out.println("chunk id : " + chunkId + " is stored on the disk");
                    }
                    else{
                        System.out.println("chunk id : " + chunkId + " is not correctly stored on the disk");
                        // should remove that from the disk
                    }
                }
                else{
                    System.out.println("network has problem");
                }
            }
        }
    }


    public void startResFixThread(){
        RequestedFixThread thread = new RequestedFixThread(this);
        thread.start();
    }

    /**
     * providing other datanode with current replicas
     */
    public void startRequested() throws IOException, NoSuchAlgorithmException {
        StorageMessages.FixDataMsg fixDataMsg = msgWrapper.getFixDataMsg();
        String filename = fixDataMsg.getFilename();
        int chunkId = fixDataMsg.getChunkId();
        String port = myHost.split(" ")[1];
        String folderPath = DataNodeCache.PATH + "/" + port + "/files/" + filename;
        File file = new File(folderPath + "/" + chunkId);
        File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
        if(io.fileIsValid(file, checkSum)){
            System.out.println("sending back to datanode to fix");
            String data = io.getFileContent(file);
            ByteString byteString = ByteString.copyFromUtf8(data);
            System.out.println("data length is: " + byteString.size());
            String sendCheckSum = io.getCheckSum(data);
            System.out.println("bytestring is: " + byteString);
            StorageMessages.FixDataMsg fixDataMsg1
                    = StorageMessages.FixDataMsg.newBuilder()
                    .setChunkId(chunkId)
                    .setData(byteString)
                    .setChecksum(sendCheckSum)
                    .setFilename(filename)
                    .setSuccess("success").build();
            StorageMessages.StorageMessageWrapper msgWrapper
                    = StorageMessages.StorageMessageWrapper.newBuilder()
                    .setFixDataMsg(fixDataMsg1)
                    .build();
            msgWrapper.writeDelimitedTo(socket.getOutputStream());
            socket.close();
        }
    }


}
