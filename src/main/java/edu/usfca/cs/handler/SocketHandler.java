package edu.usfca.cs.handler;

import com.google.protobuf.ByteString;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.memory.ClientCache;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by bingkunyang on 9/20/17.
 */
public class SocketHandler {

    private Socket socket;

    public void send(ClientCache cache, String filename, int chunkId) throws IOException {
        socket = new Socket(ClientCache.SERVER_HOST, cache.CLIENT_SERVER_PORT);
        ByteString data = ByteString.copyFromUtf8("post"); // data here is just the method
        StorageMessages.StoreChunk storeChunkMsg
                = StorageMessages.StoreChunk.newBuilder()
                .setFileName(filename)
                .setChunkId(chunkId)
                .setData(data)
                .build();

        StorageMessages.StorageMessageWrapper msgWrapper =
                StorageMessages.StorageMessageWrapper.newBuilder()
                        .setStoreChunkMsg(storeChunkMsg)
                        .build();

        msgWrapper.writeDelimitedTo(socket.getOutputStream());
        socket.close();
    }

}
