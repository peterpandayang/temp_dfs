package edu.usfca.cs.thread;

import edu.usfca.cs.handler.DataNodeHandler;
import edu.usfca.cs.route.DataNodeDataRouter;

import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeStoreThread extends Thread{

    private Socket socket;
    private DataNodeDataRouter router;

    public DataNodeStoreThread(DataNodeDataRouter router, Socket socket){
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run(){
        router.storeData();
    }

}
