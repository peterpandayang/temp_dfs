package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeFixRouter;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class RequestingFixThread extends Thread{

    private DataNodeFixRouter router;

    public RequestingFixThread(DataNodeFixRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        router.startRequsting();
    }
}
