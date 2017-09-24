package edu.usfca.cs.thread;

import edu.usfca.cs.handler.ServerSocketHandler;

import java.io.IOException;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ServerNodeListener extends Thread {

    public ServerSocketHandler handler;
    public int port;

    public ServerNodeListener(ServerSocketHandler serverSocketHandler, int port) {
        this.handler = serverSocketHandler;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            handler.serveReqFromNode(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}