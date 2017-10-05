package edu.usfca.cs.thread;


import edu.usfca.cs.route.ReplicaMaintainer;

import java.io.IOException;

/**
 * Created by bingkunyang on 10/3/17.
 */
public class ServerScanningThread extends Thread{

    private ReplicaMaintainer maintainer;

    public ServerScanningThread(ReplicaMaintainer maintainer){
        this.maintainer = maintainer;
    }

    @Override
    public void run() {
        try {
            maintainer.scanAndFix();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
