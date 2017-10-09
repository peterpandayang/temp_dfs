package edu.usfca.cs.thread;

import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;

/**
 * Created by bingkunyang on 10/8/17.
 */
public class CountStorageThread extends Thread {

    ServerReqRouter router;

    public CountStorageThread(ServerReqRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        try {
            router.sendStorageInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
