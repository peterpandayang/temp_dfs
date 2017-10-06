package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.HeartbeatSendThread;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Created by bingkunyang on 9/24/17.
 *
 * This class belongs to the DataNode for sending the heartbeat
 */

public class HeartbeatSender {

    private String myHost;
    private DataNodeCache cache;

    public HeartbeatSender(String myHost, DataNodeCache cache){
        this.myHost = myHost;
        this.cache = cache;
    }

    public void createHeartbeatSendThread() throws IOException {
        HeartbeatSendThread thread = new HeartbeatSendThread(this);
        initialSend();
        thread.start();
    }

    private void initialSend() throws IOException {
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

    public void sendHeartbeat() throws InterruptedException, IOException {
        while(true){
            Thread.sleep(5000);
            try{
                Socket socket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
                List<String> filenameAndChunkId = cache.getFilenameAndChunkId();
//            if(filenameAndChunkId.size() == 0){
//                continue;
//            }
                StorageMessages.HeartbeatMsg.Builder builder
                        = StorageMessages.HeartbeatMsg.newBuilder()
                        .setType("general")
                        .setHost(myHost)
                        .addAllFilenameChunkId(filenameAndChunkId);
                StorageMessages.HeartbeatMsg heartbeatMsg = builder.build();
                StorageMessages.StorageMessageWrapper msgWrapper =
                        StorageMessages.StorageMessageWrapper.newBuilder()
                                .setHeartbeatMsg(heartbeatMsg)
                                .build();
                msgWrapper.writeDelimitedTo(socket.getOutputStream());
                socket.close();
            }
            catch (java.net.ConnectException e){
                System.out.println("The controller is down...");
                Thread.sleep(5000);
            }
            finally{
                continue;
            }
        }
    }

}
