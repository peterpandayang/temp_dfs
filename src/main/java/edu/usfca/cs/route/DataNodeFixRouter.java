package edu.usfca.cs.route;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.RequestedFixThread;
import edu.usfca.cs.thread.RequestingFixThread;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeFixRouter {

    private Socket socket;  // this is socket from server to datanode;
    private StorageMessages.StorageMessageWrapper msgWrapper;
    private ThreadPoolExecutor threadPool;

    public DataNodeFixRouter(Socket socket, StorageMessages.StorageMessageWrapper msgWrapper, ThreadPoolExecutor threadPool){
        this.socket = socket;
        this.msgWrapper = msgWrapper;
        this.threadPool = threadPool;
    }



    public void startReqFixThread(){
        RequestingFixThread thread = new RequestingFixThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    /**
     * asking other datanode to provide the replicas
     */
    public void startRequsting() throws IOException {
        System.out.println("starting request duplica to store");
        if(msgWrapper.hasFixInfoMsg()){
            System.out.println("has fix info message from the controller");
            StorageMessages.FixInfoMsg fixInfoMsg = msgWrapper.getFixInfoMsg();
            String filenameChunkId = fixInfoMsg.getFilenameChunkId();
            String host = fixInfoMsg.getHost();
            System.out.println("get the request of fixing " + filenameChunkId + " from " + host);
            // construct the message to another datanode here...
            // this is toDataNodeSocket...
            



        }
        else{
            System.out.println("no fixing information");
        }
        // construct the return msg to the controller
        System.out.println("sending back to the server");
        StorageMessages.FixInfoMsg fixInfoMsg1 =
                StorageMessages.FixInfoMsg.newBuilder()
                        .setSuccess("success").build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setFixInfoMsg(fixInfoMsg1)
                        .build();
        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();
    }


    public void startResFixThread(){
        RequestedFixThread thread = new RequestedFixThread(this);
//        thread.start();
        threadPool.execute(thread);
    }

    /**
     * prodigin other datanode with current replicas
     */
    public void startRequested() throws IOException {
        System.out.println("start processing fixing data request");
        if(msgWrapper.hasFixDataMsg()){
            System.out.println("there is request for fixing data replica");

        }
        else{
            System.out.println("Nothing in the message wrapper");
        }
        socket.close();
    }


}
