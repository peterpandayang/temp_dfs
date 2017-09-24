package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ServerGetReqThread;
import edu.usfca.cs.thread.ServerPostReqThread;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerReqRouter {

    private static Socket socket;
    private ServerCache cache;

    public ServerReqRouter(Socket socket, ServerCache cache){
        this.socket = socket;
        this.cache = cache;
    }

    public void startPostReqThread(){
        ServerPostReqThread thread = new ServerPostReqThread(this);
        thread.start();
    }

    public void startGetReqThread(){
        ServerGetReqThread thread = new ServerGetReqThread(this);
        thread.start();
    }

    public void processPostReq() throws IOException {
        StorageMessages.RequestMsg requestMsg = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream()).getRequestMsg();
        cache.storeChunkInfo(requestMsg);
        List<String> nodes = cache.getAvailableNodeName(); // the available nodes
        StorageMessages.RequestMsg.Builder responseMsgBuilder
                = StorageMessages.RequestMsg.newBuilder();
        responseMsgBuilder.addAllHost(nodes);
        StorageMessages.StorageMessageWrapper msgWrapper
                = StorageMessages.StorageMessageWrapper.newBuilder()
                .setRequestMsg(responseMsgBuilder)
                .build();
//        String[] clientHostInfo = requestMsg.getHost(0).split(" "); // this could be used to test
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();
    }


    /**
     * get the working datanode with chunkId for the client
     */
    public void processGetReq(){



        // if there is mistake, should get a valid chunk to the client and get make a duplicate
        // for that chunk -> need to get the right replica to the client and send to DataNode.
    }

}
