package edu.usfca.cs.dfs;

import edu.usfca.cs.handler.ClientSocketHandler;
import edu.usfca.cs.handler.ServerSocketHandler;
import edu.usfca.cs.memory.ServerCache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {

    private static ServerSocketHandler socketHandler;
    private static ServerCache cache;

    public Controller(){
        cache = new ServerCache();
        socketHandler = new ServerSocketHandler(cache);
    }

    public static void main(String[] args) {
        Controller server = new Controller();
        server.socketHandler.listenClient();
        server.socketHandler.listenNode();
    }

}
