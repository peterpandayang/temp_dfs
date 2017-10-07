package edu.usfca.cs.route;

import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.thread.RequestedFixThread;
import edu.usfca.cs.thread.RequestingFixThread;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
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
    public void startRequsting() throws IOException, InterruptedException {
        System.out.println("starting request duplica to store");
        if(msgWrapper.hasFixInfoMsg()){
            System.out.println("has fix info message from the controller");
            StorageMessages.FixInfoMsg fixInfoMsg = msgWrapper.getFixInfoMsg();
            String filenameChunkId = fixInfoMsg.getFilenameChunkId();
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
            File checkSum = new File(folderPath + "/" + chunkId + ".checksum");
            if(io.fileIsValid(file, checkSum)){
                System.out.println("The checksum test has pass locally");
            }
            else{
                System.out.println("The chunk has been corrupted");
            }

            // construct the message
            StorageMessages.FixDataMsg returnMsg =
                    StorageMessages.FixDataMsg.newBuilder()
                    .setFilenameChunkId(filenameChunkId)
//                    .setData()
                    .setChecksum("here")
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


}
