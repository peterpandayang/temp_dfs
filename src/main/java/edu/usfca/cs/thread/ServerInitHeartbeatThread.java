package edu.usfca.cs.thread;

import edu.usfca.cs.route.HeartbeatRouter;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the server that handles the first heartbeat to update the DataNode's info
 *
 */
public class ServerInitHeartbeatThread extends Thread{

    private HeartbeatRouter router;

    public ServerInitHeartbeatThread(HeartbeatRouter router){
        this.router = router;
    }

    @Override
    public void run(){
        try {
            router.initDataNodeStatus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
