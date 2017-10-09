package edu.usfca.cs.route;

import edu.usfca.cs.cache.ClientCache;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 10/8/17.
 */
public class StorageRequester {

    public StorageRequester(){

    }

    public void requestStorage() throws IOException {
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        StorageMessages.RequestMsg requestMsg =
                StorageMessages.RequestMsg.newBuilder()
                .setType("storage").build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                .setRequestMsg(requestMsg).build();
        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        System.out.println("sending message of asking storage");
        // should get something from the other side...



        toServerSocket.close();
    }

}
