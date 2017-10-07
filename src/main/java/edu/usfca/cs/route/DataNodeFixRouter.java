package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.thread.DataNodeSendLogThread;
import edu.usfca.cs.thread.RequestedFixThread;
import edu.usfca.cs.thread.RequestingFixThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeFixRouter {

    private Socket socket;  // this is socket from server to datanode;
    private StorageMessages.StorageMessageWrapper msgWrapper;
    private ThreadPoolExecutor threadPool;
    private DataNodeCache cache;
    private String myHost;
    private FileIO io = new FileIO();


    public DataNodeFixRouter(Socket socket, StorageMessages.StorageMessageWrapper msgWrapper, ThreadPoolExecutor threadPool, DataNodeCache cache, String myHost){
        this.socket = socket;
        this.msgWrapper = msgWrapper;
        this.threadPool = threadPool;
        this.cache = cache;
        this.myHost = myHost;
    }



    public void startReqFixThread(){
        RequestingFixThread thread = new RequestingFixThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    /**
     * asking other datanode to provide the replicas
     */
    public void startRequsting() throws IOException, InterruptedException, NoSuchAlgorithmException {
        System.out.println("starting request duplica to store");
        if(msgWrapper.hasFixInfoMsg()){
            System.out.println("has fix info message from the controller");
            StorageMessages.FixInfoMsg fixInfoMsg = msgWrapper.getFixInfoMsg();
            String filenameChunkId = fixInfoMsg.getFilenameChunkId();
            String[] fileChunks = filenameChunkId.split(" ");
            String filename = fileChunks[0];
            int chunkId = Integer.parseInt(fileChunks[1]);
            String host = fixInfoMsg.getHost();
            System.out.println("get the request of fixing " + filenameChunkId + " from " + host);
            // construct the message to another datanode here...
            // this is toDataNodeSocket...
            String[] hosts = host.split(" ");
            Socket toDataNodeSocket = new Socket(hosts[0], Integer.parseInt(hosts[1]));
            StorageMessages.FixDataMsg fixDataMsg =
                    StorageMessages.FixDataMsg.newBuilder()
                    .setFilenameChunkId(filenameChunkId).build();
            StorageMessages.StorageMessageWrapper msgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                    .setFixDataMsg(fixDataMsg).build();
            msgWrapper.writeDelimitedTo(toDataNodeSocket.getOutputStream());
            StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(toDataNodeSocket.getInputStream());
            int attempt = 0;
            while(returnMsgWrapper == null && attempt <= 999){
                attempt++;
                returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(toDataNodeSocket.getInputStream());
                Thread.sleep(10);
            }
            if(returnMsgWrapper == null){
                System.out.println("nothing from the datanode...");
            }
            else{
                System.out.println("get data from the other storage node...");
                String port = myHost.split(" ")[1];
                StorageMessages.FixDataMsg fixDataMsg1 = returnMsgWrapper.getFixDataMsg();
                String data = fixDataMsg1.getData().toStringUtf8();
                String folderPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files/" + filename;
                if(!Files.exists(Paths.get(folderPath))){
                    Files.createDirectories(Paths.get(folderPath));
                }
                File file = new File(folderPath + "/" + chunkId);
                io.writeGeneralFile(file, data);
                File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
                io.writeGeneralFile(checkSum, fixDataMsg1.getChecksum());
                String logPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files";
                if(!Files.exists(Paths.get(logPath))){
                    Files.createDirectories(Paths.get(logPath));
                }
                if(io.fileIsValid(file, checkSum)){
                    cache.updateFileInfo(filename, chunkId);
                    System.out.println("file " + filename + "'s " + "chunk" + chunkId + " has been successfully fixed on this machine");
                    io.writeLog(filename, chunkId, logPath + "/log");
                }
                else{
                    System.out.println("chunk id : " + chunkId + " is not correctly stored on the disk");
                    // should remove that from the disk
                }
            }
            toDataNodeSocket.close();
        }
        else{
            System.out.println("no fixing information");
        }
        // construct the return msg to the controller
        System.out.println("sending back to the server");
        StorageMessages.FixInfoMsg fixInfoMsg1 =
                StorageMessages.FixInfoMsg.newBuilder()
                        .setSuccess("success").build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setFixInfoMsg(fixInfoMsg1)
                        .build();
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();
    }


    public void startResFixThread(){
        RequestedFixThread thread = new RequestedFixThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    /**
     * prodigin other datanode with current replicas
     */
    public void startRequested() throws IOException, NoSuchAlgorithmException {
        System.out.println("start processing fixing data request");
        if(msgWrapper.hasFixDataMsg()){
            System.out.println("there is request for fixing data replica");
            // should provide the other datanode with duplica here
            StorageMessages.FixDataMsg fixDataMsg = msgWrapper.getFixDataMsg();
            String filenameChunkId = fixDataMsg.getFilenameChunkId();
            String[] fileChunks = filenameChunkId.split(" ");
            String filename = fileChunks[0];
            int chunkId = Integer.parseInt(fileChunks[1]);
            System.out.println("Load the file " + filename + "'s chunk " + chunkId + " and send back to the asking node");
            // get the data and check here
            String port = myHost.split(" ")[1];
            String folderPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files/" + filename;
            File file = new File(folderPath + "/" + chunkId);
            File checkSumFile = new File(folderPath + "/" + chunkId + ".checksum");
            if(io.fileIsValid(file, checkSumFile)){
                System.out.println("The checksum test has pass locally");
            }
            else{
                System.out.println("The chunk has been corrupted");
            }
            String data = io.getFileContent(file);
            String checkSum = io.getFileContent(checkSumFile);
            ByteString byteString = ByteString.copyFromUtf8(data);
            // construct the message
            StorageMessages.FixDataMsg returnMsg =
                    StorageMessages.FixDataMsg.newBuilder()
                    .setFilenameChunkId(filenameChunkId)
                    .setData(byteString)
                    .setChecksum(checkSum)
                    .build();
            StorageMessages.StorageMessageWrapper msgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                    .setFixDataMsg(returnMsg).build();
            msgWrapper.writeDelimitedTo(socket.getOutputStream());
        }
        else{
            System.out.println("Nothing in the message wrapper ");
        }
        socket.close();
    }

    public void startSendLogThread(){
        DataNodeSendLogThread thread = new DataNodeSendLogThread(this);
        threadPool.execute(thread);
    }

    public void sendLogToController(){
        System.out.println("start to send back the log");
        // should firstly read the log and send them back
        String type = msgWrapper.getHeartbeatMsg().getType();
        if(type.equals(type)){
            System.out.println("type is " + type);
        }
        else{
            System.out.println("there is some other types");
        }
    }


}
