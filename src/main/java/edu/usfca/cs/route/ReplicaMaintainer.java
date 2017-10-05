package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.thread.ServerScanningThread;

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

    public void scanAndFix() throws InterruptedException {
//        while(true){
//            Thread.sleep(10000);
//            Map<String, List<String>> fixMap = cache.getMaintainMap();
//            for(String s : )
//        }
    }

}
