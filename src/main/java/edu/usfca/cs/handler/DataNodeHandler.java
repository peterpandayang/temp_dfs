package edu.usfca.cs.handler;

import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.route.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeHandler {

    private ServerSocket datanodeSocket;
    private String hostname;
    private DataNodeCache cache;
    private String myHost;
    private static ThreadPoolExecutor threadPool;

    public DataNodeHandler(String hostname){
        this.hostname = hostname;
        cache = new DataNodeCache();
    }

    public void initThreadPool(){
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public void closeThreadPool(){
        int activeThreads = threadPool.getActiveCount();
        while(activeThreads != 0){
            activeThreads = threadPool.getActiveCount();
        }
        threadPool.shutdown();
    }

    public void start() throws IOException, NoSuchAlgorithmException {

        datanodeSocket = new ServerSocket(0);
        int port = datanodeSocket.getLocalPort();
        myHost = hostname + " " + port;

        // create a thread and send the heartbeat
        HeartbeatSender heartbeatSender = new HeartbeatSender(myHost, cache);
        heartbeatSender.createHeartbeatSendThread();

        initThreadPool();

        // start to listen
        while (true) {
            Socket socket = datanodeSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if(msgWrapper.hasDataMsg()){
                DataNodeDataRouter dataNodeDataRouter = new DataNodeDataRouter(cache, socket, myHost, msgWrapper, threadPool);
                String type = msgWrapper.getDataMsg().getType();
                if(type.equals("store")){
                    System.out.println("get the store request from the client...");
                    dataNodeDataRouter.startStoreDataThread();
                }
                else{
                    dataNodeDataRouter.startGetDataThread();
                }
            }
            else if(msgWrapper.hasHeartbeatMsg()){
                // this is unnecessary
                continue;
            }
            else if(msgWrapper.hasRequestMsg()){
                if(msgWrapper.getRequestMsg().getType().equals("remove")){
                    System.out.println("receive request msg from the server");
                    if(msgWrapper.getRequestMsg().getFilename().equals("all")){
                        System.out.println("remove all files");
                        // here should handle the removal
                        DataNodeDataRemover dataNodeDataRemover = new DataNodeDataRemover(cache, socket, threadPool);
                        dataNodeDataRemover.startRemoveAllFileThread();
                    }
                    else{
                        System.out.println("some other remove request");
                    }
                }
                else{
                    System.out.println("nothing in the request...");
                }
            }
            else if(msgWrapper.hasFixInfoMsg()){ // current datanode will ask other datanode for replica
                DataNodeFixRouter dataNodeFixRouter = new DataNodeFixRouter(socket, msgWrapper, threadPool, cache, myHost);
                dataNodeFixRouter.startReqFixThread();
            }
            else if(msgWrapper.hasFixDataMsg()){ // current datanode will proved the replica
                DataNodeFixRouter dataNodeFixRouter = new DataNodeFixRouter(socket, msgWrapper, threadPool, cache, myHost);
                dataNodeFixRouter.startResFixThread();
            }
        }

    }

}
