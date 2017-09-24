package edu.usfca.cs.route;

import edu.usfca.cs.thread.RequestedFixThread;
import edu.usfca.cs.thread.RequestingFixThread;

import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeFixRouter {

    Socket socket;  // this is socket from server to datanode;

    public DataNodeFixRouter(Socket socket){
        this.socket = socket;
    }

    public void startReqFixThread(){
        RequestingFixThread thread = new RequestingFixThread(this);
        thread.start();
    }

    /**
     * asking other datanode to provide the replicas
     */
    public void startRequsting(){

    }


    public void startResFixThread(){
        RequestedFixThread thread = new RequestedFixThread(this);
        thread.start();
    }

    /**
     * prodigin other datanode with current replicas
     */
    public void startRequested(){

    }


}
