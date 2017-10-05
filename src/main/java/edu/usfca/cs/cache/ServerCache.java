package edu.usfca.cs.cache;

import edu.usfca.cs.dfs.StorageMessages;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerCache {

    private Map<String, TreeMap<Integer, List<String>>> dataMap;  // helpful when retrieving file
    private Map<String, Integer> nodeHostMap; // maps the node hostname and the port
    private List<String> active;                //record the active node host
    private Map<String, Long> lastHeartbeat; // this map record the last heartbeat for each active host
    Random random = new Random();

    public ServerCache(){
        dataMap = new ConcurrentHashMap<>();
        nodeHostMap = new ConcurrentHashMap<>();
        active = new ArrayList<>();
        lastHeartbeat = new HashMap<>();
    }

    public synchronized void initNodeHost(StorageMessages.HeartbeatMsg heartbeatMsg){
        String host = heartbeatMsg.getHost();
        String[] nodeHost = host.split(" ");
        nodeHostMap.put(nodeHost[0], Integer.parseInt(nodeHost[1]));
        long currentTime = System.currentTimeMillis();
        lastHeartbeat.put(host,currentTime);
        active.add(host);
        System.out.println("current time is : " + currentTime);
        System.out.println(nodeHostMap.toString());
    }

    public void storeChunkInfo(StorageMessages.RequestMsg requestMsg){
        String filename = requestMsg.getFilename();
        int chunkId = requestMsg.getChunkId();
        if(!dataMap.containsKey(filename)){
            dataMap.put(filename, new TreeMap<Integer, List<String>>());
        }
        TreeMap<Integer, List<String>> idMap = dataMap.get(filename);
        if(!idMap.containsKey(chunkId)){
            idMap.put(chunkId, new ArrayList<String>());
        }
    }

    /**
     * get the avalaible node for the client
     * @return
     */
    public synchronized List<String> getAvailableNodeName() {
        List<String> randomNode = new ArrayList<>();
        while(true){
            int index = random.nextInt(active.size());
            String node = active.get(index);
            if(!randomNode.contains(node)){
                randomNode.add(node);
            }
            // if the # of node to store has reach the active node limit or meet the requirement for minimal replicas
            if(randomNode.size() == Math.min(GeneralCache.DEFAULT_REPLICAS, active.size())){
                break;
            }
        }
        List<String> nodeNames = new ArrayList<>();
        for(int i = 0; i <= randomNode.size() - 1; i++){
            nodeNames.add(randomNode.get(i) + " " + nodeHostMap.get(randomNode.get(i)));
        }
        return nodeNames;
    }


    /**
     * update the prev hearbeat time for each node
     * if there is one node down, return false.
     * @param heartbeatMsg
     * @return
     */
    public synchronized boolean updateActiveNode(StorageMessages.HeartbeatMsg heartbeatMsg) {
        long currentTime = System.currentTimeMillis();
        String host = heartbeatMsg.getHost();
        boolean hasDown = false;
        if(lastHeartbeat.containsKey(host)){
            lastHeartbeat.put(host, currentTime);
            List<String> removeList = new ArrayList<>();
            for(int i = 0; i <= active.size() - 1; i++){
                long prevTime = lastHeartbeat.get(active.get(i));
                long diff = (currentTime - prevTime) / 1000;
                if(diff > 15){
                    removeList.add(active.get(i));
                    hasDown = true;
                }
            }
            for(int i = 0; i <= removeList.size() - 1; i++){
                String currHost = removeList.get(i);
                active.remove(currHost);
                lastHeartbeat.remove(currHost);
                System.out.println("remove host " + currHost + " from the list");
            }
            for(int i = 0; i <= active.size() - 1; i++){
                System.out.println("active node is: " + active.get(i));
            }
        }
        else{
            // the shutdown node restart...

        }
        return hasDown;
    }

    public synchronized void updateFileInfo(String host, List<String> list) {
        System.out.println("update some information from heartbeat...");
        for(String info : list){
            String[] infos = info.split(" ");
            String filename = infos[0];
            int chunkId = Integer.parseInt(infos[1]);
            if(!dataMap.containsKey(filename)){
                dataMap.put(filename, new TreeMap());
            }
            TreeMap treeMap = dataMap.get(filename);
            if(!treeMap.containsKey(chunkId)){
                treeMap.put(chunkId, new ArrayList());
            }
            ((List)treeMap.get(chunkId)).add(host);
            System.out.println("store " + filename + "'s chunkId: " + chunkId + " into host: " + host);
        }
    }


    public synchronized List<String> constructChunkIdAndHostList(String filename) {
        TreeMap treeMap = dataMap.get(filename);
        List<String> rst = new ArrayList<>();
        for(Object id : treeMap.keySet()){
            int chunkId = (int)id;
            List<String> potentialNodes = (List<String>) treeMap.get(chunkId);
            StringBuilder sb = new StringBuilder();
            sb.append(chunkId).append(" ");
            for(String s : potentialNodes){
                if(active.contains(s)){
                    sb.append(s);
                    break;
                }
            }
            rst.add(sb.toString());
        }
        return rst;
    }

    public Map<String, List<String>> getMaintainMap(){
        // this method will get the chunk that needs to be fixed and
        // the value in the map contains the following information:
        // <host1(valid chunk), host2(replica destination), ...>
        Map<String, List<String>> map = new HashMap<>();
        for(String filename : dataMap.keySet()){
            TreeMap treeMap = dataMap.get(filename);
            for(Object id : treeMap.keySet()){
                int chunkId = (int)id;
                List<String> hosts = (List<String>) treeMap.get(chunkId);
                if(hosts.size() != Math.min(active.size(), 3)){
                    int i = random.nextInt(hosts.size());
                    String askedHost = hosts.get(i);
                    List<String> fixList = new ArrayList<>();
                    fixList.add(askedHost);
                    while(fixList.size() != Math.min(active.size(), 3)){
                        i = random.nextInt(active.size());
                        String askingHost = active.get(i);
                        if(!fixList.contains(askingHost) && !hosts.contains(askingHost)){
                            fixList.add(askingHost);
                        }
                    }
                    String filenameChunkId = filename + " " + chunkId;
                    map.put(filenameChunkId, fixList);
                }
            }
        }
        return map;
    }


}
