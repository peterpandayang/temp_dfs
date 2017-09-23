package edu.usfca.cs.thread;

import edu.usfca.cs.handler.ServerSocketHandler;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ServerNodeListener extends Thread {

    public ServerSocketHandler serverSocketHandler;
    public int PORT;

    public ServerNodeListener(ServerSocketHandler serverSocketHandler, int PORT) {
        this.serverSocketHandler = serverSocketHandler;
        this.PORT = PORT;
    }

    @Override
    public void run() {
        try {
            serverSocketHandler.serveReqFromNode(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}