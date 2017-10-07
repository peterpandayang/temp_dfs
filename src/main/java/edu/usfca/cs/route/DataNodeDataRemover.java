package edu.usfca.cs.route;

import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.thread.DataNodeRemoveAllThread;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 10/6/17.
 */
public class DataNodeDataRemover {

    DataNodeCache cache;
    Socket socket;
    ThreadPoolExecutor threadPool;

    public DataNodeDataRemover(DataNodeCache cache, Socket socket, ThreadPoolExecutor threadPool){
        this.cache = cache;
        this.socket = socket;
        this.threadPool = threadPool;
    }

    public void startRemoveAllFileThread(){
        DataNodeRemoveAllThread thread = new DataNodeRemoveAllThread(this);
        threadPool.execute(thread);
    }

    public void removeAllFile() throws IOException {
        System.out.println("start removing datanode files...");
        // there should start to do the removal part
        String folderPath = cache.pathPrefix;
        if(Files.exists(Paths.get(folderPath))){
            Files.delete(Paths.get(folderPath));
        }
        System.out.println("all files have been deleted");
        socket.close();
    }
}
