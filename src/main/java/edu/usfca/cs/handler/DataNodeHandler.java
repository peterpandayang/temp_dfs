package edu.usfca.cs.handler;

import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.route.DataNodeDataRouter;
import edu.usfca.cs.route.DataNodeFixRouter;
import edu.usfca.cs.route.HeartbeatSender;
import edu.usfca.cs.route.ServerReqRouter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeHandler {

    private ServerSocket datanodeSocket;
    private String hostname;
    private DataNodeCache cache;

    public DataNodeHandler(String hostname){
        this.hostname = hostname;
        cache = new DataNodeCache();
    }

    public void start() throws IOException {

        datanodeSocket = new ServerSocket(0);
        int port = datanodeSocket.getLocalPort();
        String myHost = hostname + " " + port;

        // create a thread and send the heartbeat
        HeartbeatSender heartbeatSender = new HeartbeatSender(myHost, cache);
        heartbeatSender.createHeartbeatSendThread();

        // start to listen
        while (true) {
            Socket socket = datanodeSocket.accept();
            StorageMessages.StorageMessageWrapper msgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
            if(msgWrapper.hasDataMsg()){
                DataNodeDataRouter dataNodeDataRouter = new DataNodeDataRouter(socket, myHost, msgWrapper);
                String type = msgWrapper.getDataMsg().getType();
                if(type.equals("store")){
                    dataNodeDataRouter.startStoreDataThread();
                }
                else{
                    dataNodeDataRouter.startGetDataThread();
                }
            }
            else if(msgWrapper.hasHeartbeatMsg()){
                // this is unnecessary
                continue;
            }
            else if(msgWrapper.hasFixInfoMsg()){ // current datanode will ask other datanode for replica
                DataNodeFixRouter dataNodeFixRouter = new DataNodeFixRouter(socket, msgWrapper);
                dataNodeFixRouter.startReqFixThread();
            }
            else if(msgWrapper.hasFixDataMsg()){ // current datanode will proved the replica
                DataNodeFixRouter dataNodeFixRouter = new DataNodeFixRouter(socket, msgWrapper);
                dataNodeFixRouter.startResFixThread();
            }
        }
    }

}
