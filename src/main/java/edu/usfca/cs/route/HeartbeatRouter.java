package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.thread.ServerGeneralHeartbeatThread;
import edu.usfca.cs.thread.ServerInitHeartbeatThread;

import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the server.
 *
 */
public class HeartbeatRouter {

    private Socket socket;
    private ServerCache cache;

    public HeartbeatRouter(Socket socket, ServerCache cache) {
        this.socket = socket;
        this.cache = cache;
    }

    public void startInitHeartbeatThread(){
        ServerInitHeartbeatThread thread = new ServerInitHeartbeatThread(this);
        thread.start();
    }

    public void updateDataNodeHost(){

    }

    public void startGeneralHeartbeatThread(){
        ServerGeneralHeartbeatThread thread = new ServerGeneralHeartbeatThread(this);
        thread.start();
    }

    public void updateDataNodeStatus(){


        // should do sonething if the datanode is down
    }

}
