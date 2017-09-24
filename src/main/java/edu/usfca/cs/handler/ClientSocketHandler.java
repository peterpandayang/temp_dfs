package edu.usfca.cs.handler;

import com.google.protobuf.ByteString;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.memory.ClientCache;
import edu.usfca.cs.thread.ClientNodeListener;
import edu.usfca.cs.thread.ClientServerListener;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by bingkunyang on 9/20/17.
 */
public class ClientSocketHandler {

    public static final int DEFAULT_NODE_STORAGE = 10;
    private ServerSocket fromServerSocket; // getting request
    private ServerSocket[] fromNodeSocket;
    private ClientServerListener serverListener; // means client side server listener
    private ClientNodeListener[] nodeListeners = new ClientNodeListener[DEFAULT_NODE_STORAGE];
    private ClientCache cache;

    public ClientSocketHandler(ClientCache cache){
        this.cache = cache;
        serverListener = new ClientServerListener(this);
        for(int i = 0; i <= DEFAULT_NODE_STORAGE - 1; i++){
            nodeListeners[i] = new ClientNodeListener(this, cache.CLIENT_STORAGE_PORTS[i]);
        }
    }

    public void start() throws IOException {
        startClient();
    }

    /**
     * This is preprocessing part for the usr's input
     * @throws IOException
     */
    private void startClient() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while (true) {
            System.out.println("please enter your command...");
            line = scanner.nextLine();
            if (line.equals("EOF")) {
                break;
            }
            if (cache.parse(line)) {
                String method = line.split(" ")[0].toLowerCase();
                if(method.equals("post")){
                    splitFile(line.split(" ")[1].toLowerCase());
                }
                else{

                }
            }
        }
    }


    /**
     * send request based on the # of the chunks
     * @param filename
     * @throws IOException
     */
    private void splitFile(String filename) throws IOException {
        // check if the file exist
        File file = new File(cache.FILE_PATH + filename);
        if(file.exists()){
            InputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream bin = new BufferedInputStream(inputStream);
            int byteread;
            int chunkId = 0;
            while((byteread = bin.read(buffer)) != -1){
                String content = new String(buffer, 0, byteread);
                sendReqToServer(cache, filename, chunkId, content);
                chunkId++;
            }
        }
        else{
            System.out.println("The file you want to store does not exists");
        }
    }


    /**
     * send the actual request to server
     * @param cache
     * @param filename
     * @param chunkId
     * @throws IOException
     */
    public void sendReqToServer(ClientCache cache, String filename, int chunkId, String content) throws IOException {
        Socket toServerSocket = new Socket(ClientCache.SERVER_HOST, cache.CLIENT_SERVER_PORT);
        ByteString data = ByteString.copyFromUtf8(content); // data here is just the empty
        StorageMessages.StoreChunk storeChunkMsg
                = StorageMessages.StoreChunk.newBuilder()
                .setFileName(filename)
                .setChunkId(chunkId)
                .setData(data)
                .setHostname(cache.hostname)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setStoreChunkMsg(storeChunkMsg)
                        .build();

        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        toServerSocket.close();
    }


    /**
     * Create a thread and listen on the server
     */
    public void listenServer(){
        serverListener.start();
    }



    public void getResFromServer() throws IOException {
        fromServerSocket = new ServerSocket(cache.SERVER_CLIENT_PORT);
        while (true) {
            Socket socket = fromServerSocket.accept();
            StorageMessages.StorageMessageWrapper inputMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            List<String> storageNodes = new ArrayList<>();
            storageNodes.add(inputMsgWrapper.getStoreChunkMsg().getStoreNode1());
            storageNodes.add(inputMsgWrapper.getStoreChunkMsg().getStoreNode2());
            storageNodes.add(inputMsgWrapper.getStoreChunkMsg().getStoreNode3());
            for(int i = 0; i <= storageNodes.size() - 1; i++){
                if(inputMsgWrapper.hasStoreChunkMsg()){
                    StorageMessages.StoreChunk chunkMsg = inputMsgWrapper.getStoreChunkMsg();
                    StorageMessages.StoreChunk msg
                            = StorageMessages.StoreChunk.newBuilder()
                            .setFileName(chunkMsg.getFileName())
                            .setChunkId(chunkMsg.getChunkId())
                            .setData(chunkMsg.getData())
                            .build();
                    StorageMessages.StorageMessageWrapper msgWrapper =
                            StorageMessages.StorageMessageWrapper.newBuilder()
                                    .setStoreChunkMsg(msg)
                                    .build();
                    String[] address = storageNodes.get(i).split(" ");
//                    Socket toNodeSocket = new Socket(address[0], Integer.parseInt(address[1]));
                    Socket toNodeSocket = new Socket("localhost", Integer.parseInt(address[1]));
//                    msgWrapper.writeDelimitedTo(toNodeSocket.getOutputStream());
//                    toNodeSocket.close();
                }
            }
        }
    }


    /**
     * Create a thread and listen on the storage node
     */
    public void listenNode(){
        for(int i = 0; i <= DEFAULT_NODE_STORAGE - 1; i++){
            nodeListeners[i].start();
        }
    }


    public void serveReqFromStorageNode(int port) throws IOException {
        fromServerSocket = new ServerSocket(port);
//        while(true){
//
//        }
    }

}
