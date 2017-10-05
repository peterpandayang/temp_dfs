package edu.usfca.cs.thread;

import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerGetReqThread extends Thread{

    ServerReqRouter router;

    public ServerGetReqThread(ServerReqRouter router){
        this.router = router;
    }

    @Override
    public void run(){
        try {
            router.processGetReq();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
