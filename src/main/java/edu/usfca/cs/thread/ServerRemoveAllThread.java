package edu.usfca.cs.thread;

import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;

/**
 * Created by bingkunyang on 10/6/17.
 */
public class ServerRemoveAllThread  extends Thread{
    private ServerReqRouter serverReqRouter;

    public ServerRemoveAllThread(ServerReqRouter serverReqRouter){
        this.serverReqRouter = serverReqRouter;
    }

    @Override
    public void run() {
        try {
            serverReqRouter.sendRemoveAllCmd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
