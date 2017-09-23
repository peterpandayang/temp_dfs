package edu.usfca.cs.handler;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.memory.ServerCache;
import edu.usfca.cs.thread.ServerClientListener;
import edu.usfca.cs.thread.ServerNodeListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ServerSocketHandler {

    public static final int DEFAULT_NODE_STORAGE = 10;

    ServerSocket fromClientSocket;
    ServerClientListener clientListener;
    ServerNodeListener[] nodeListener = new ServerNodeListener[DEFAULT_NODE_STORAGE];
    ServerCache cache;

    public ServerSocketHandler(ServerCache cache){
        this.cache = cache;
        clientListener = new ServerClientListener(this);
        for(int i = 0; i <= 9; i++){
            nodeListener[i] = new ServerNodeListener(this, cache.SERVER_STORAGE_PORTS[i]);
        }
    }

    public void listenClient(){
        clientListener.start();
    }

    public void listenNode(){
        for(int i = 0; i <= 9; i++){
            nodeListener[i].start();
        }
    }

    public void serveReqFromClient() throws IOException {
        System.out.println("start listening on client...");
        fromClientSocket = new ServerSocket(cache.CLIENT_SERVER_PORT);
        while (true) {
            Socket socket = fromClientSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if (msgWrapper.hasStoreChunkMsg()) {
                StorageMessages.StoreChunk storeChunkMsg = msgWrapper.getStoreChunkMsg();
                System.out.println("Storing file name: " + storeChunkMsg.getFileName());
            }
        }

    }

    public void serveReqFromNode(int port) throws IOException {
        System.out.println("start listening storage node with post: " + port);
        fromClientSocket = new ServerSocket(port);
        while (true) {
            Socket socket = fromClientSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if (msgWrapper.hasStoreChunkMsg()) {
                StorageMessages.StoreChunk storeChunkMsg = msgWrapper.getStoreChunkMsg();
                System.out.println("Storing file name: " + storeChunkMsg.getFileName());
            }
        }

    }



}
