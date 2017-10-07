package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeFixRouter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
        try {
            router.startRequsting();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
