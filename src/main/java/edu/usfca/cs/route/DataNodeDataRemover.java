package edu.usfca.cs.route;

import edu.usfca.cs.cache.DataNodeCache;
import edu.usfca.cs.thread.DataNodeRemoveAllThread;

import java.io.File;
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
        File file = new File(folderPath);
        deleteFolder(file);
        System.out.println("all files have been deleted");
        socket.close();
    }


    /**
     * Refer link: https://stackoverflow.com/questions/7768071/how-to-delete-directory-content-in-java
     * @param folder
     */
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
