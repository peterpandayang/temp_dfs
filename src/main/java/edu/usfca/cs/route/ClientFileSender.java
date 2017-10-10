package edu.usfca.cs.route;

import com.google.protobuf.ByteString;
import edu.usfca.cs.cache.GeneralCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.io.FileIO;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientFileSender {

    private String myHostname;
    private String filename;
    private int chunkId;
    private String data;
    private FileIO io = new FileIO();

    public ClientFileSender(String myHostname, String filename, int chunkId, String data){
        this.myHostname = myHostname;
        this.filename = filename;
        this.data = data;
        this.chunkId = chunkId;
    }

    public void startPostReq() throws IOException, InterruptedException, NoSuchAlgorithmException {
        sendPostReq();
    }

    /**
     * send the actual request to server
     * @throws IOException
     */
    public void sendPostReq() throws IOException, InterruptedException, NoSuchAlgorithmException {
        Socket toServerSocket = new Socket(GeneralCache.SERVER_HOSTNAME, GeneralCache.SERVER_PORT);
        String host = myHostname + " " + toServerSocket.getLocalPort();
        StorageMessages.RequestMsg.Builder builder
                = StorageMessages.RequestMsg.newBuilder()
                .setFilename(filename)
                .setChunkId(chunkId)
                .setType("post");
        builder.addHostBytes(ByteString.copyFromUtf8(host));
        StorageMessages.RequestMsg requestMsg = builder.build();
        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setRequestMsg(requestMsg)
                        .build();
        msgWrapper.writeDelimitedTo(toServerSocket.getOutputStream());
        InputStream inputStream = toServerSocket.getInputStream();
        StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(inputStream);
        int attempt = 0;
        while(returnMsgWrapper == null && attempt <= 999){
            attempt++;
            Thread.sleep(10);
            returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(toServerSocket.getInputStream());
        }

        if(returnMsgWrapper != null){
            // make a new socket to connect to the datanode
            List<String> datanodeList = returnMsgWrapper.getRequestMsg().getHostList();
            for(String node : datanodeList){
                sendDataToDataNode(node);
            }
        }

        toServerSocket.close();
    }


    private void sendDataToDataNode(String node) throws IOException, NoSuchAlgorithmException {
        String[] nodeInfo = node.split(" ");
        Socket nodeSocket = new Socket(nodeInfo[0], Integer.parseInt(nodeInfo[1]));
        ByteString byteString = ByteString.copyFromUtf8(data);
        String checksum = io.getCheckSum(data);
        StorageMessages.DataMsg.Builder dataMsgBuilder
                = StorageMessages.DataMsg.newBuilder()
                .setChunkId(chunkId)
                .setData(byteString)
                .setFilename(filename)
                .setChecksum(checksum)
                .setType("store");
        StorageMessages.StorageMessageWrapper dataMsgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setDataMsg(dataMsgBuilder.build())
                        .build();
        System.out.println("sending chunk : " + chunkId + " for file: " + filename);
        dataMsgWrapper.writeDelimitedTo(nodeSocket.getOutputStream());
        nodeSocket.close();
    }


}
