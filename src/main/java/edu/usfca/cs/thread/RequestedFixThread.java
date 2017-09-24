package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeFixRouter;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class RequestedFixThread extends Thread{

    private DataNodeFixRouter router;

    public RequestedFixThread(DataNodeFixRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        router.startRequested();
    }

}
