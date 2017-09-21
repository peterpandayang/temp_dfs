package edu.usfca.cs.control;

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

    public void write(ClientCache cache, String filename, int chunkId) throws IOException {
        socket = new Socket(cache.SERVER_HOST, cache.CLIENT_SERVER_PORT);
        ByteString data = ByteString.copyFromUtf8("Hello World!");
        StorageMessages.StoreChunk storeChunkMsg
                = StorageMessages.StoreChunk.newBuilder()
                .setFileName("my_file.txt")
                .setChunkId(3)
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
