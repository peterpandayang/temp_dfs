package edu.usfca.cs.thread;

import edu.usfca.cs.handler.DataNodeHandler;
import edu.usfca.cs.route.DataNodeDataRouter;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

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
        try {
            router.storeData(socket);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
