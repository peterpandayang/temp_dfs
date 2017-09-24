package edu.usfca.cs.route;

import edu.usfca.cs.thread.DataNodeStoreThread;

import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeDataRouter {

    private static Socket socket;
    private static String myHost;

    public DataNodeDataRouter(Socket socket, String myHost){
        this.socket = socket;
        this.myHost = myHost;
    }

    public void startStoreDataThread(){
        DataNodeStoreThread thread = new DataNodeStoreThread(this, socket);
        thread.start();
    }


    public void storeData(){
        // this is the real part that store the data
    }

    public void startGetDataThread(){

    }

    public void getData(){


        // this is the real part that get the data and do the checksum.


    }


}
