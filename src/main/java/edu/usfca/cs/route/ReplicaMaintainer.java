package edu.usfca.cs.route;

import edu.usfca.cs.cache.ServerCache;
import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.thread.ServerScanningThread;

import java.io.IOException;
import java.net.Socket;
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
    private ConcurrentHashMap<String, List<String>> fixMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> tempMap = new ConcurrentHashMap<>();


    public ReplicaMaintainer(ServerCache cache, ThreadPoolExecutor threadPool){
        this.cache = cache;
        this.threadPool = threadPool;
    }

    public void startScanningThread(){
        ServerScanningThread thread = new ServerScanningThread(this);
        threadPool.execute(thread);
    }

    public void scanAndFix() throws InterruptedException, IOException {
        while(true){
            while(cache.started){
                Thread.sleep(5000);
            }
            Thread.sleep(20000);
            System.out.println("start scanning..............................................................................................................................");
            fixMap = cache.getMaintainMap();
            int counter = 0;

            if(fixMap.size() == 0){
                System.out.println("no duplica needs to be fixed");
                if(tempMap.size() != 0){
                    System.out.println("You notice some duplica are missing previously because the system needs time to adjust and now it is fine to use, sorry for any inconvenience");
                }
                tempMap = new ConcurrentHashMap<>();
            }
            else{
                if(tempMap.size() == 0 ){
                    System.out.println("There might have some problems, let's wait another 20sec and see...");
                    tempMap = new ConcurrentHashMap<>(fixMap);
                    continue;
                }
                else{
                    System.out.println("we have some missing duplica, but don't worry, we'll fix them for you");
                    sendFixInfo(fixMap);
                    if(fixMap.size() == 0){
                        System.out.println("all duplica has been fixed");
                        tempMap = new ConcurrentHashMap<>();
                        break;
                    }
//                    if(counter == 3){
//                        System.out.println("Has been waiting for 15 sec and some chunks has not been fixed.");
//                        break;
//                    }
                }

            }
            if(fixMap.size() != 0){
                System.out.println("scan again for insufficient chunk duplica");
            }
        }
    }

    private void sendFixInfo(ConcurrentHashMap<String, List<String>> fixMap) throws IOException, InterruptedException {
        int fixNumber = fixMap.size();
        for(String filenameChunkId : fixMap.keySet()){
            List<String> hosts = fixMap.get(filenameChunkId);
            String askedHost = hosts.get(0);
            String askingHost = hosts.get(1);
            String[] askingHosts = askingHost.split(" ");
            try {
                Socket socket = new Socket(askingHosts[0], Integer.parseInt(askingHosts[1]));
                StorageMessages.FixInfoMsg fixInfoMsg =
                        StorageMessages.FixInfoMsg.newBuilder()
                                .setFilenameChunkId(filenameChunkId)
                                .setHost(askedHost).build();
                StorageMessages.StorageMessageWrapper msgWrapper =
                        StorageMessages.StorageMessageWrapper.newBuilder()
                                .setFixInfoMsg(fixInfoMsg)
                                .build();
                msgWrapper.writeDelimitedTo(socket.getOutputStream());
                // get response from the server
                StorageMessages.StorageMessageWrapper returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
                int attempt = 0;
                while (returnMsgWrapper == null && attempt <= 999) {
                    attempt++;
                    returnMsgWrapper = StorageMessages.StorageMessageWrapper.parseDelimitedFrom(socket.getInputStream());
                    Thread.sleep(10);
                }
                StorageMessages.RequestMsg returnRequestMsg = null;
                if (returnMsgWrapper != null) {
                    System.out.println("get response from the fixing datanode...");
                    returnRequestMsg = returnMsgWrapper.getRequestMsg();
                }

                //            Thread.sleep(1000);
                socket.close();
            }
            catch (java.net.ConnectException e){
                System.out.println("The node" + askingHost + " is down...");
            }
            finally{
                continue;
            }
        }

        // should have all the response from the datanode even of they do not fix it!!!

    }

}
