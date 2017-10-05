package edu.usfca.cs.dfs;

import edu.usfca.cs.handler.DataNodeHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class StorageNode {

    private static DataNodeHandler handler;

    public StorageNode() throws UnknownHostException {
        String hostname = getHostname();
        handler = new DataNodeHandler(hostname);
    }

    public static void main(String[] args) throws Exception {
        StorageNode storageNode = new StorageNode();
        System.out.println("DataNode start listening...");
        handler.start();
    }

    /**
     * Retrieves the short host name of the current host.
     *
     * @return name of the current host
     */
    private static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }

}
