package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.RemoveFileInfoThread;
import edu.usfca.cs.thread.ServerScanningThread;
import sun.awt.windows.ThemeReader;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 10/3/17.
 */
public class ReplicaMaintainer {

    private ServerCache cache;
    private ThreadPoolExecutor threadPool;

    public ReplicaMaintainer(ServerCache cache, ThreadPoolExecutor threadPool){
        this.cache = cache;
        this.threadPool = threadPool;
    }

    public void startScanningThread(){
        ServerScanningThread thread = new ServerScanningThread(this);
        threadPool.execute(thread);
    }

    public void startRemoveFileInfoThread(StorageMessages.FixInfoMsg fixInfoMsg){
        RemoveFileInfoThread thread = new RemoveFileInfoThread(this, fixInfoMsg);
        threadPool.execute(thread);
    }

    public void removeCorruptedFileInfo(StorageMessages.FixInfoMsg fixInfoMsg){
        String filename = fixInfoMsg.getFilename();
        int chunkId = fixInfoMsg.getChunkId();
        String host = fixInfoMsg.getHost();
        cache.removeFromDataMap(filename, chunkId, host);
    }

    public void scanAndFix() throws InterruptedException, IOException {
        while(true){
            // scan every 10 sec
            Thread.sleep(10000);
            System.out.println("start scanning...");
            // the getMaintainMap method will get the chunk that needs to be fixed and
            // the value in the map contains the following information:
            // <filenameChunkId, <host1(valid chunk), host2(replica destination), ...>>
            Map<String, List<String>> fixMap = cache.getMaintainMap();
            for(String filenameChunkId : fixMap.keySet()){
                System.out.println("some chunks needs to be fixed...");
                String[] fixFileInfo = filenameChunkId.split(" ");
                String filename = fixFileInfo[0];
                int chunkId = Integer.parseInt(fixFileInfo[1]);
                List<String> list = fixMap.get(filenameChunkId);
                String askedNode = list.get(0);
                for(int i = 1; i <= list.size() - 1; i++){
                    String askingNode = list.get(i);
                    System.out.println("node " + askingNode + " is asking data from node " + askedNode);
                    String[] askingHost = askingNode.split(" ");
                    Socket socket = new Socket(askingHost[0], Integer.parseInt(askingHost[1]));
                    StorageMessages.FixInfoMsg fixInfoMsg
                            = StorageMessages.FixInfoMsg.newBuilder()
                            .setHost(askedNode)
                            .setFilename(filename)
                            .setChunkId(chunkId).build();
                    StorageMessages.StorageMessageWrapper msgWrapper
                            = StorageMessages.StorageMessageWrapper.newBuilder()
                            .setFixInfoMsg(fixInfoMsg)
                            .build();
                    msgWrapper.writeTo(socket.getOutputStream());
                    Thread.sleep(1000);
                    socket.close();
                }
            }
        }
    }

}
