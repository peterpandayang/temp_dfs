package edu.usfca.cs.route;

import edu.usfca.cs.thread.HeartbeatSendThread;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the DataNode for sending the heartbeat
 */

public class HeartbeatSender {

    private String myHost;

    public HeartbeatSender(String myHost){
        this.myHost = myHost;
    }

    public void createHeartbeatSendThread(){
        HeartbeatSendThread thread = new HeartbeatSendThread(this);
        initialSend(myHost);
        thread.start();
    }

    private void initialSend(String myHost) {

    }

    public void sendHeartbeat(){

    }

}
