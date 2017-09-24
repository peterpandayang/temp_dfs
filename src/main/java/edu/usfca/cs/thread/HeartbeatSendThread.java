package edu.usfca.cs.thread;

import edu.usfca.cs.route.HeartbeatSender;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class HeartbeatSendThread extends Thread{

    private HeartbeatSender sender;

    public HeartbeatSendThread(HeartbeatSender sender){
        this.sender = sender;
    }

    @Override
    public void run(){
        sender.sendHeartbeat();
    }

}
