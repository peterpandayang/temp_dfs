package edu.usfca.cs.thread;

import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerPostReqThread extends Thread{
    private ServerReqRouter router;

    public ServerPostReqThread(ServerReqRouter router){
        this.router = router;
    }

    @Override
    public void run(){
        try {
            router.processPostReq();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
