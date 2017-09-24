package edu.usfca.cs.route;

import edu.usfca.cs.thread.ClientGetReqThread;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientFileRetriever {

    private String filename;

    public ClientFileRetriever(String filename){
        this.filename = filename;
    }

    public void startGetReqThread(){
        ClientGetReqThread thread = new ClientGetReqThread(this);
        thread.start();
    }

    public void sendGetReq(){
        // this is the main logic


        // if the data returned is empty, should ask the server again
    }

}
