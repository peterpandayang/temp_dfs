package edu.usfca.cs.route;

import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 10/6/17.
 */
public class ClientRemoveCmdSender {

    private String target;

    public ClientRemoveCmdSender(String target){
        this.target = target;
    }

    public void startRemoveAll() throws IOException {
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        StorageMessages.RequestMsg requestMsg =
                StorageMessages.RequestMsg.newBuilder()
                .setType("remove")
                .setFilename(target).build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                .setRequestMsg(requestMsg).build();
        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        toServerSocket.close();
    }

}
