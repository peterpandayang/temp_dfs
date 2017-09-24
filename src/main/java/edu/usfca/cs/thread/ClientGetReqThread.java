package edu.usfca.cs.thread;

import edu.usfca.cs.route.ClientFileRetriever;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientGetReqThread extends Thread{

    private ClientFileRetriever retriever;

    public ClientGetReqThread(ClientFileRetriever retriever){
        this.retriever = retriever;
    }

    @Override
    public void run() {
        retriever.sendGetReq();
    }
}
