package edu.usfca.cs.dfs;

import edu.usfca.cs.handler.SocketHandler;
import edu.usfca.cs.memory.ServerCache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {

    private ServerCache cache;
    private SocketHandler socketHandler;

    public Controller(){
        cache = new ServerCache();
        socketHandler = new SocketHandler();
    }

    public static void main(String[] args) {
        Controller server = new Controller();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException {
        System.out.println("Starting controller...");
        ServerSocket srvSocket = new ServerSocket(cache.CLIENT_SERVER_PORT);
        while (true) {
            Socket socket = srvSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if (msgWrapper.hasStoreChunkMsg()) {
                StorageMessages.StoreChunk storeChunkMsg = msgWrapper.getStoreChunkMsg();
                System.out.println("Storing file name: " + storeChunkMsg.getFileName());
            }
        }
    }

}
