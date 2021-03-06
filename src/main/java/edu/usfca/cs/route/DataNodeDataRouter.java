package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.handler.ErrorChecker;
import edu.usfca.cs.io.*;
import edu.usfca.cs.thread.DataNodeGetThread;
import edu.usfca.cs.thread.DataNodeStoreThread;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeDataRouter {

    private static Socket socket;
    private static String myHost;
    private StorageMessages.StorageMessageWrapper msgWrapper;
    private DataNodeCache cache;
    private FileIO io;
    private ThreadPoolExecutor threadPool;
    private ErrorChecker checker = new ErrorChecker();

    public DataNodeDataRouter(DataNodeCache cache, Socket socket, String myHost, StorageMessages.StorageMessageWrapper msgWrapper, ThreadPoolExecutor threadPool){
        this.cache = cache;
        DataNodeDataRouter.socket = socket;
        DataNodeDataRouter.myHost = myHost;
        this.msgWrapper = msgWrapper;
        io = new FileIO();
        this.threadPool = threadPool;
    }

    public void startStoreDataThread() throws IOException, NoSuchAlgorithmException {
        DataNodeStoreThread thread = new DataNodeStoreThread(this, socket);
        threadPool.execute(thread);
    }


    public void storeData(Socket socket) throws NoSuchAlgorithmException, IOException {
        // this is the real part that store the data
        StorageMessages.DataMsg dataMsg = msgWrapper.getDataMsg();
        int level = msgWrapper.getDataMsg().getLevel();
        String port = myHost.split(" ")[1];
        String filename = dataMsg.getFilename();
        int chunkId = dataMsg.getChunkId();
        String data = dataMsg.getData().toStringUtf8();
        checker.check(io.getCheckSum(data), dataMsg.getChecksum(), "network", "datanode");
        // later I will not use the port as the path but use hostname.
//        String folderPath = DataNodeCache.PATH + "/" + port + "/files/" + filename;
        String folderPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files/" + filename;
        String logPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files";
        if(!Files.exists(Paths.get(folderPath))){
            Files.createDirectories(Paths.get(folderPath));
        }
        // update the map here for heartbeat...
        File file = new File(folderPath + "/" + chunkId);
        io.writeGeneralFile(file, data);
        File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
        io.writeGeneralFile(checkSum, dataMsg.getChecksum());
        StorageMessages.DataMsg.Builder builder;
        if(!Files.exists(Paths.get(logPath))){
            Files.createDirectories(Paths.get(logPath));
        }
        if(io.fileIsValid(file, checkSum)){
            cache.updateFileInfo(filename, chunkId);
            System.out.println("chunk id : " + chunkId + " is stored on the disk");
            builder = StorageMessages.DataMsg.newBuilder().setSuccess("success");
            io.writeLog(filename, chunkId, logPath + "/log");
        }
        else{
            System.out.println("chunk id : " + chunkId + " is not correctly stored on the disk");
            builder = StorageMessages.DataMsg.newBuilder().setSuccess("failed");
            // should remove that from the disk
        }
        StorageMessages.StorageMessageWrapper returnMsgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setDataMsg(builder.build())
                        .build();
        returnMsgWrapper.writeDelimitedTo(socket.getOutputStream());
        if(level != 3){
            List<String> hosts = msgWrapper.getDataMsg().getHostsList();
            Socket nextSocket = null;
            StorageMessages.DataMsg nextMsg = null;
            if(level == 1){
                String nextHost = hosts.get(0);
                List<String> temp = new ArrayList<>();
                temp.add(hosts.get(1));
                String[] nextHosts = nextHost.split(" ");
                nextSocket = new Socket(nextHosts[0], Integer.parseInt(nextHosts[1]));
                nextMsg = StorageMessages.DataMsg.newBuilder()
                                .setData(dataMsg.getData())
                                .setFilename(dataMsg.getFilename())
                                .setChunkId(dataMsg.getChunkId())
                                .setChecksum(dataMsg.getChecksum())
                                .setLevel(2).addAllHosts(temp).setType("store").build();
            }
            else if(level == 2){
                String nextHost = hosts.get(0);
                String[] nextHosts = nextHost.split(" ");
                nextSocket = new Socket(nextHosts[0], Integer.parseInt(nextHosts[1]));
                nextMsg = StorageMessages.DataMsg.newBuilder()
                                .setData(dataMsg.getData())
                                .setFilename(dataMsg.getFilename())
                                .setChunkId(dataMsg.getChunkId())
                                .setChecksum(dataMsg.getChecksum())
                                .setLevel(3).setType("store").build();
            }
            System.out.println("current level is : " + level);
            StorageMessages.StorageMessageWrapper nextMsgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                    .setDataMsg(nextMsg).build();
            System.out.println("sending to next node");
            nextMsgWrapper.writeDelimitedTo(nextSocket.getOutputStream());
            nextSocket.close();
        }
        socket.close();
    }

    public void startGetDataThread(){
        DataNodeGetThread thread = new DataNodeGetThread(this, socket);
        threadPool.execute(thread);
    }

    public void getData(Socket socket) throws NoSuchAlgorithmException, IOException {
        // this is the real part that get the data and do the checksum.
        StorageMessages.DataMsg dataMsg = msgWrapper.getDataMsg();
        String port = myHost.split(" ")[1];
        String filename = dataMsg.getFilename();
        int chunkId = dataMsg.getChunkId();
        // later I will not use the port as the path but use hostname.
//        String folderPath = DataNodeCache.PATH + "/" + port + "/files/" + filename;
        String folderPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files/" + filename;
        Files.createDirectories(Paths.get(folderPath));
        File file = new File(folderPath + "/" + chunkId);
        File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
        if(io.fileIsValid(file, checkSum)){
            // create message write data to the socket
            System.out.println("start to write to the client...");
            String data = io.getFileContent(file);
            ByteString byteString = ByteString.copyFromUtf8(data);
            String sendCheckSum = io.getCheckSum(data);
            System.out.println("bytestring is: " + byteString);
            StorageMessages.DataMsg.Builder dataMsgBuilder
                    = StorageMessages.DataMsg.newBuilder()
                    .setChunkId(chunkId)
                    .setData(byteString)
                    .setChecksum(sendCheckSum)
                    .setFilename(filename);
            StorageMessages.StorageMessageWrapper msgWrapper
                    = StorageMessages.StorageMessageWrapper.newBuilder()
                    .setDataMsg(dataMsgBuilder)
                    .build();
            msgWrapper.writeDelimitedTo(socket.getOutputStream());
            socket.close();
        }
        else{
            System.out.println("This chunk has been corrupted");
            // create message and write not valid to socket
            StorageMessages.DataMsg returnMsg =
                    StorageMessages.DataMsg.newBuilder()
                    .setSuccess("false").build();
            StorageMessages.StorageMessageWrapper msgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                    .setDataMsg(returnMsg).build();
            msgWrapper.writeDelimitedTo(socket.getOutputStream());
            socket.close();
            // should remove that from the disk
            String logPath = cache.pathPrefix + DataNodeCache.PATH + "/" + port + "/files";
            io.removeFilenamChunkId(filename, chunkId, logPath + "/log");
        }
    }


}
