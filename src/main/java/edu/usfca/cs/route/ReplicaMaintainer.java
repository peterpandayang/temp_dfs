package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.thread.ServerScanningThread;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 10/3/17.
 */
public class ReplicaMaintainer {

    private ServerCache cache;
    private ThreadPoolExecutor threadPool;
    private ConcurrentHashMap<String, List<String>> fixMap;

    public ReplicaMaintainer(ServerCache cache, ThreadPoolExecutor threadPool){
        this.cache = cache;
        this.threadPool = threadPool;
    }

    public void startScanningThread(){
        ServerScanningThread thread = new ServerScanningThread(this);
        threadPool.execute(thread);
    }

    public void scanAndFix() throws InterruptedException {
        while(true){
            Thread.sleep(10000);
            System.out.println("start scanning...");
            fixMap = cache.getMaintainMap();
            System.out.println("waiting for chunk to be fixed...");
            int counter = 0;
            while(fixMap.size() != 0){
                System.out.println("still waiting...");
                Thread.sleep(5000);
                counter++;
                if(counter == 3){
                    System.out.println("Has been waiting for 15 sec and some chunks has not been fixed.");
                }
            }
            if(fixMap.size() == 0){
                System.out.println("all chunk has been fixed");
            }
            else{
                System.out.println("scan again for insufficient chunk duplica");
            }
        }
    }

}
