package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeDataRouter;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeGetThread extends Thread{

    DataNodeDataRouter router;

    public DataNodeGetThread(DataNodeDataRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        router.getData();
    }
}
