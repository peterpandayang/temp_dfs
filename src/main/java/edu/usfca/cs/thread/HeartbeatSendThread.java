package edu.usfca.cs.thread;

import edu.usfca.cs.route.HeartbeatSender;

import java.io.IOException;

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
        try {
            sender.sendHeartbeat();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
