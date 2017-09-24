package edu.usfca.cs.thread;

import edu.usfca.cs.route.ClientFileSender;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientPostReqThread extends Thread{

    public ClientFileSender sender;

    public ClientPostReqThread(ClientFileSender sender){
        this.sender = sender;
    }

    @Override
    public void run(){
        try {
            sender.sendPostReq();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
