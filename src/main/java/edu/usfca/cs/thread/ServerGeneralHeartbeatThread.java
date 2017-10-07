package edu.usfca.cs.thread;

import edu.usfca.cs.route.HeartbeatRouter;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the server that handles the general heartbeat from the DataNode
 *
 */
public class ServerGeneralHeartbeatThread extends Thread{

    HeartbeatRouter router;

    public ServerGeneralHeartbeatThread(HeartbeatRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        try {
            router.updateDataNodeStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
