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
    StorageMessages.StorageMessageWrapper msgWrapper;

    public ServerReqRouter(Socket socket, ServerCache cache, StorageMessages.StorageMessageWrapper msgWrapper){
        this.socket = socket;
        this.cache = cache;
        this.msgWrapper = msgWrapper;
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

    }


    /**
     * get the working datanode with chunkId for the client
     */
    public void processGetReq(){



        // if there is mistake, should get a valid chunk to the client and get make a duplicate
        // for that chunk -> need to get the right replica to the client and send to DataNode.
    }

}
