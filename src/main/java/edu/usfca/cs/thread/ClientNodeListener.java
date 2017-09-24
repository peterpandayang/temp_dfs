package edu.usfca.cs.thread;

import edu.usfca.cs.handler.ClientSocketHandler;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ClientNodeListener extends Thread{

    private ClientSocketHandler handler;
    private int port;

    public ClientNodeListener(ClientSocketHandler handler, int port){
        this.handler = handler;
        this.port = port;
    }

    @Override
    public void run(){
        try {
            handler.serveReqFromStorageNode(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
