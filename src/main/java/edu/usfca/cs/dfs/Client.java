package edu.usfca.cs.dfs;

import edu.usfca.cs.handler.ClientSideHandler;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client {

    private static String hostname;
    private static ClientSideHandler handler;

    public Client() throws UnknownHostException {
        hostname = getHostname();
        handler = new ClientSideHandler(hostname);
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
        handler.start();
    }

}
