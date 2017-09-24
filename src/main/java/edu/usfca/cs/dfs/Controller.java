package edu.usfca.cs.dfs;


import edu.usfca.cs.handler.ServerSideHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Controller {

    private static ServerSideHandler handler;

    public Controller(){
        handler = new ServerSideHandler();
    }

    public static void main(String[] args) throws IOException {
        Controller server = new Controller();
        handler.start();
    }

}
