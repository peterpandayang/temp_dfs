package edu.usfca.cs.dfs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import edu.usfca.cs.handler.ClientSocketHandler;
import edu.usfca.cs.memory.ClientCache;


public class Client {

    private static ClientCache cache;
//    private static ClientFileHandler fileHandler;
    private static ClientSocketHandler socketHandler;

    public Client() {
        try {
            cache = new ClientCache(getHostname());
            socketHandler = new ClientSocketHandler(cache);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the short host name of the current host.
     *
     * @return name of the current host
     */
    private static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.socketHandler.listenServer();
        client.socketHandler.listenNode();
        client.socketHandler.start();
    }

}
