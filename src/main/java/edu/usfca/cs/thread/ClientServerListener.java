package edu.usfca.cs.thread;

import edu.usfca.cs.handler.ClientSocketHandler;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ClientServerListener extends Thread{
    ClientSocketHandler handler;

    public ClientServerListener(ClientSocketHandler handler){
        this.handler = handler;
    }

    @Override
    public void run(){
        try {
            handler.getResFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
