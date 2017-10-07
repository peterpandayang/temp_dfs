package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ServerGetReqThread;
import edu.usfca.cs.thread.ServerPostReqThread;
import edu.usfca.cs.thread.ServerRemoveAllThread;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerReqRouter {

    private static Socket socket;
    private ServerCache cache;
    StorageMessages.StorageMessageWrapper msgWrapper;

    public ServerReqRouter(Socket socket, ServerCache cache, StorageMessages.StorageMessageWrapper msgWrapper){
        this.socket = socket;
        this.cache = cache;
        this.msgWrapper = msgWrapper;
    }

    public void startPostReqThread(){
        System.out.println("start the thread...");
        ServerPostReqThread thread = new ServerPostReqThread(this);
        System.out.println("process the request...");
        thread.start();
    }

    public void startGetReqThread(){
        ServerGetReqThread thread = new ServerGetReqThread(this);
        thread.start();
    }

    public void processPostReq() throws IOException {
        cache.storeChunkInfo(msgWrapper.getRequestMsg());
        List<String> nodes = cache.getAvailableNodeName(); // the available nodes
        StorageMessages.RequestMsg.Builder responseMsgBuilder
                = StorageMessages.RequestMsg.newBuilder();
        responseMsgBuilder.addAllHost(nodes);
        StorageMessages.StorageMessageWrapper msgWrapper
                = StorageMessages.StorageMessageWrapper.newBuilder()
                .setRequestMsg(responseMsgBuilder)
                .build();
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();
    }


    /**
     * get the working datanode with chunkId for the client
     */
    public void processGetReq() throws IOException {
        StorageMessages.RequestMsg getMsg = msgWrapper.getRequestMsg();
        String filename = getMsg.getFilename();
        List<String> chunkIdAndHostList = cache.constructChunkIdAndHostList(filename);
        StorageMessages.RequestMsg.Builder responseMsgBuilder
                = StorageMessages.RequestMsg.newBuilder();
        responseMsgBuilder.addAllChunkIdHost(chunkIdAndHostList);
        StorageMessages.StorageMessageWrapper msgWrapper
                = StorageMessages.StorageMessageWrapper.newBuilder()
                .setRequestMsg(responseMsgBuilder)
                .build();
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();


        // if there is mistake, should get a valid chunk to the client and get make a duplicate
        // for that chunk -> need to get the right replica to the client and send to DataNode.
    }


    public void startRemoveProcess(String target){
        if(target.equals("-rf")){
            System.out.println("start remove all thread");
            ServerRemoveAllThread thread = new ServerRemoveAllThread(this);
            thread.start();
        }
    }

    public void sendRemoveAllCmd() throws IOException {
        System.out.println("sending remove request to the datanode...");
        List<String> list = new ArrayList<>(cache.getActiveNode());
        for(String host : list){
            String[] hosts = host.split(" ");
            Socket toNodeSocket = new Socket(hosts[0], Integer.parseInt(hosts[1]));
            StorageMessages.RequestMsg requestMsg =
                    StorageMessages.RequestMsg.newBuilder()
                            .setType("remove")
                            .setFilename("all")
                            .build();
            StorageMessages.StorageMessageWrapper msgWrapper =
                    StorageMessages.StorageMessageWrapper.newBuilder()
                            .setRequestMsg(requestMsg).build();
            msgWrapper.writeDelimitedTo(toNodeSocket.getOutputStream());
            toNodeSocket.close();
        }
        cache.clearDataMap();
        socket.close();
    }

}
