package edu.usfca.cs.thread;

import edu.usfca.cs.route.ServerReqRouter;

/**
 * Created by bingkunyang on 10/9/17.
 */
public class ServerRemoveOneChunkInfo extends Thread {

    private ServerReqRouter router ;

    public ServerRemoveOneChunkInfo(ServerReqRouter router){
        this.router = router;
    }

    @Override
    public void run() {
        router.startRemoveOneChunk();
    }
}
