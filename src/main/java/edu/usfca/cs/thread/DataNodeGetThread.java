package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeDataRouter;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeGetThread extends Thread{

    DataNodeDataRouter router;
    Socket socket;

    public DataNodeGetThread(DataNodeDataRouter router, Socket socket){
        this.router = router;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            router.getData(socket);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
