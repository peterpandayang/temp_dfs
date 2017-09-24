package edu.usfca.cs.handler;

import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.route.HeartbeatRouter;
import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerSideHandler {

    ServerSocket serverSocket;
    ServerCache cache;

    public ServerSideHandler(){
        cache = new ServerCache();
    }

    public void start() throws IOException {
        System.out.println("Server start listening...");
        serverSocket = new ServerSocket(GeneralCache.SERVER_PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if(msgWrapper.hasRequestMsg()){
                ServerReqRouter serverReqRouter = new ServerReqRouter(socket, cache);
                String type = msgWrapper.getRequestMsg().getType();
                if(type.equals("post")){
                    serverReqRouter.startPostReqThread();
                }
                else{
                    serverReqRouter.startGetReqThread();
                }
            }
            else if(msgWrapper.hasHeartbeatMsg()){
                HeartbeatRouter heartbeatRouter = new HeartbeatRouter(socket, cache);
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
