package edu.usfca.cs.thread;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.handler.ServerSocketHandler;
import edu.usfca.cs.memory.ServerCache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/23/17.
 */
public class ServerClientListener extends Thread{

    ServerSocketHandler serverSocketHandler;

    public ServerClientListener(ServerSocketHandler serverSocketHandler){
        this.serverSocketHandler = serverSocketHandler;
    }

    @Override
    public void run(){
        try {
            serverSocketHandler.serveReqFromClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
