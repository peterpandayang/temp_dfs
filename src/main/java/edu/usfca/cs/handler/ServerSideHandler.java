package edu.usfca.cs.handler;

import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.route.HeartbeatRouter;
import edu.usfca.cs.route.ReplicaMaintainer;
import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerSideHandler {

    ServerSocket serverSocket;
    ServerCache cache;
    private static ThreadPoolExecutor threadPool;

    public ServerSideHandler(){
        cache = new ServerCache();
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

    public void start() throws IOException {

        initThreadPool();
        ReplicaMaintainer maintainer = new ReplicaMaintainer(cache, threadPool);
        maintainer.startScanningThread();

        System.out.println("Server start listening...");
        serverSocket = new ServerSocket(GeneralCache.SERVER_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if(msgWrapper.hasRequestMsg()){
                ServerReqRouter serverReqRouter = new ServerReqRouter(socket, cache, msgWrapper, threadPool);
                String type = msgWrapper.getRequestMsg().getType();
                if(type.equals("post")){
                    serverReqRouter.startPostReqThread();
                }
                else if(type.equals("get")){
                    serverReqRouter.startGetReqThread();
                }
                else if(type.equals("remove")){
                    serverReqRouter.startRemoveProcess(msgWrapper.getRequestMsg().getFilename());
                }
                else if(type.equals("storage")){
                    System.out.println("get the request of asking storage");
                    serverReqRouter.startCountStorageThread();
                }
                else{
                    // handle the put request later...
                }
            }
            else if(msgWrapper.hasHeartbeatMsg()){
                HeartbeatRouter heartbeatRouter = new HeartbeatRouter(socket, cache, msgWrapper, threadPool);
                String type = msgWrapper.getHeartbeatMsg().getType();
                if(type.equals("init")){
                    heartbeatRouter.startInitHeartbeatThread();
                }
                else{
                    heartbeatRouter.startGeneralHeartbeatThread();
                }
            }
        }
    }


}
