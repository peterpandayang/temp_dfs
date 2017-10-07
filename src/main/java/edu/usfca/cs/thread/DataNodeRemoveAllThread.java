package edu.usfca.cs.thread;

import edu.usfca.cs.route.DataNodeDataRemover;

/**
 * Created by bingkunyang on 10/6/17.
 */
public class DataNodeRemoveAllThread extends Thread {

    DataNodeDataRemover dataNodeDataRemover;

    public DataNodeRemoveAllThread(DataNodeDataRemover dataNodeDataRemover){
        this.dataNodeDataRemover = dataNodeDataRemover;
    }

    @Override
    public void run() {
        dataNodeDataRemover.removeAllFile();
    }
}
