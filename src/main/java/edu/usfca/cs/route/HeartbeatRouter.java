package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ServerGeneralHeartbeatThread;
import edu.usfca.cs.thread.ServerInitHeartbeatThread;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

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
    private ThreadPoolExecutor threadPool;

    public HeartbeatRouter(Socket socket, ServerCache cache, StorageMessages.StorageMessageWrapper msgWrapper, ThreadPoolExecutor threadPool) {
        this.socket = socket;
        this.cache = cache;
        this.msgWrapper = msgWrapper;
        this.threadPool = threadPool;
    }

    public void startInitHeartbeatThread(){
        ServerInitHeartbeatThread thread = new ServerInitHeartbeatThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    public void initDataNodeStatus() throws IOException {
        cache.initNodeHost(msgWrapper.getHeartbeatMsg());
        socket.close();
    }

    public void startGeneralHeartbeatThread(){
        ServerGeneralHeartbeatThread thread = new ServerGeneralHeartbeatThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    public void updateDataNodeStatus() throws IOException {
        StorageMessages.HeartbeatMsg heartbeatMsg = msgWrapper.getHeartbeatMsg();
        boolean nodeDown = cache.updateActiveNode(heartbeatMsg);
        if(nodeDown){
            // should do something if the datanode is down
            System.out.println("Some node is down... ");
        }
        // should update the node info
        String host = heartbeatMsg.getHost();
        List<String> list = heartbeatMsg.getFilenameChunkIdList();
        if(list.size() != 0){
            cache.updateFileInfo(host, list);
        }
        else{
            System.out.println("This heartbeat is empty...");
        }
    }

}
