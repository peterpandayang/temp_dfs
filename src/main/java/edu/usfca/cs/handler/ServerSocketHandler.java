package edu.usfca.cs.handler;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.memory.ServerCache;
import edu.usfca.cs.thread.ServerClientListener;
import edu.usfca.cs.thread.ServerNodeListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ServerSocketHandler {

    public static final int DEFAULT_NODE_STORAGE = 10;

    ServerSocket fromClientSocket;
    ServerClientListener clientListener;
    ServerNodeListener[] nodeListeners = new ServerNodeListener[DEFAULT_NODE_STORAGE];
    ServerCache cache;
    Random random = new Random();

    public ServerSocketHandler(ServerCache cache){
        this.cache = cache;
        clientListener = new ServerClientListener(this);
        for(int i = 0; i <= DEFAULT_NODE_STORAGE - 1; i++){
            nodeListeners[i] = new ServerNodeListener(this, cache.SERVER_STORAGE_PORTS[i]);
        }
    }

    public void listenClient(){
        clientListener.start();
    }

    public void listenNode(){
        for(int i = 0; i <= DEFAULT_NODE_STORAGE - 1; i++){
            nodeListeners[i].start();
        }
    }

    public void serveReqFromClient() throws IOException {
        System.out.println("start listening on client...");
        fromClientSocket = new ServerSocket(cache.CLIENT_SERVER_PORT);
        while (true) {
            Socket socket = fromClientSocket.accept();
            StorageMessages.StorageMessageWrapper inputMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if(inputMsgWrapper.hasStoreChunkMsg()){
                StorageMessages.StoreChunk chunkMsg = inputMsgWrapper.getStoreChunkMsg();
                cache.initialize(inputMsgWrapper.getStoreChunkMsg());
                List<String> nodeNames = cache.getAvailableNodeName();
                StorageMessages.StoreChunk msg
                        = StorageMessages.StoreChunk.newBuilder()
                        .setFileName(chunkMsg.getFileName())
                        .setChunkId(chunkMsg.getChunkId())
                        .setStoreNode1(nodeNames.get(0))
                        .setStoreNode2(nodeNames.get(1))
                        .setStoreNode3(nodeNames.get(2))
                        .setData(chunkMsg.getData())
                        .build();
                StorageMessages.StorageMessageWrapper msgWrapper =
                        StorageMessages.StorageMessageWrapper.newBuilder()
                                .setStoreChunkMsg(msg)
                                .build();
                Socket toClientSocket = new Socket(inputMsgWrapper.getStoreChunkMsg().getHostname(), cache.SERVER_CLIENT_PORT);
                msgWrapper.writeDelimitedTo(toClientSocket.getOutputStream());
                toClientSocket.close();
            }
        }

    }


    public void serveReqFromNode(int port) throws IOException {
        System.out.println("start listening storage node with post: " + port + "...");
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
