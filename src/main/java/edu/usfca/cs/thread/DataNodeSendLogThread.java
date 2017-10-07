package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeFixRouter;

import java.io.IOException;

/**
 * Created by bingkunyang on 10/7/17.
 */
public class DataNodeSendLogThread extends Thread {

    private DataNodeFixRouter router;

    public DataNodeSendLogThread(DataNodeFixRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        try {
            router.sendLogToController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
