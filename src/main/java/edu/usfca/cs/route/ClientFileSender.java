package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ClientPostReqThread;

import java.io.*;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientFileSender {

    private String myHostname;
    private String filename;
    private int chunkId;
    private String data;

    public ClientFileSender(String myHostname, String filename, int chunkId, String data){
        this.myHostname = myHostname;
        this.filename = filename;
        this.data = data;
        this.chunkId = chunkId;
    }

    public void startPostReqThread(){
        ClientPostReqThread thread = new ClientPostReqThread(this);
        thread.start();
    }


    /**
     * send the actual request to server
     * @throws IOException
     */
    public void sendPostReq() throws IOException {
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
//        ByteString data = ByteString.copyFromUtf8(content); // data here is just the empty
        String host = myHostname + " " + toServerSocket.getLocalPort();
        StorageMessages.RequestMsg.Builder builder
                = StorageMessages.RequestMsg.newBuilder()
                .setFilename(filename)
                .setChunkId(chunkId);
        builder.addHostBytes(ByteString.copyFromUtf8(host));
        StorageMessages.RequestMsg requestMsg = builder.build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setRequestMsg(requestMsg)
                        .build();
        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        toServerSocket.setSoTimeout(5000);
        if(toServerSocket.getInputStream() != null){
            // send msg to the datanode
        }
        toServerSocket.close();
    }


}
