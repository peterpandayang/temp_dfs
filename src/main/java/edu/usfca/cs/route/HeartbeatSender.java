package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.HeartbeatSendThread;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the DataNode for sending the heartbeat
 */

public class HeartbeatSender {

    private String myHost;

    public HeartbeatSender(String myHost){
        this.myHost = myHost;
    }

    public void createHeartbeatSendThread() throws IOException {
        HeartbeatSendThread thread = new HeartbeatSendThread(this);
        initialSend(myHost);
        thread.start();
    }

    private void initialSend(String myHost) throws IOException {
        Socket socket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        StorageMessages.HeartbeatMsg.Builder builder
                = StorageMessages.HeartbeatMsg.newBuilder()
                .setHost(myHost)
                .setType("init");
        StorageMessages.HeartbeatMsg heartbeatMsg = builder.build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setHeartbeatMsg(heartbeatMsg)
                        .build();
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();

    }

    public void sendHeartbeat(){

    }

}
