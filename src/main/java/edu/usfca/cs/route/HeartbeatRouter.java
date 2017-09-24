package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ServerGeneralHeartbeatThread;
import edu.usfca.cs.thread.ServerInitHeartbeatThread;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the server.
 *
 */
public class HeartbeatRouter {

    private Socket socket;
    private ServerCache cache;
    private StorageMessages.StorageMessageWrapper msgWrapper;

    public HeartbeatRouter(Socket socket, ServerCache cache, StorageMessages.StorageMessageWrapper msgWrapper) {
        this.socket = socket;
        this.cache = cache;
        this.msgWrapper = msgWrapper;
    }

    public void startInitHeartbeatThread(){
        ServerInitHeartbeatThread thread = new ServerInitHeartbeatThread(this);
        thread.start();
    }

    public void initDataNodeStatus() throws IOException {
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

    public void startGeneralHeartbeatThread(){
        ServerGeneralHeartbeatThread thread = new ServerGeneralHeartbeatThread(this);
        thread.start();
    }

    public void updateDataNodeStatus(){


        // should do sonething if the datanode is down
    }

}
