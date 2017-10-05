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
    public synchronized List<String> updateActiveNode(StorageMessages.HeartbeatMsg heartbeatMsg) {
        long currentTime = System.currentTimeMillis();
        String host = heartbeatMsg.getHost();
        List<String> removeList = new ArrayList<>();
        if(lastHeartbeat.containsKey(host)){
            lastHeartbeat.put(host, currentTime);
            for(int i = 0; i <= active.size() - 1; i++){
                long prevTime = lastHeartbeat.get(active.get(i));
                long diff = (currentTime - prevTime) / 1000;
                if(diff > 15){
                    removeList.add(active.get(i));
                    System.out.println("Datanode " + active.get(i) + " is down...");
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
        return removeList;
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
                    sb.append(s).append(" ");
                }
            }
            rst.add(sb.toString().trim());
        }
        return rst;
    }

    public Map<String, List<String>> getMaintainMap(){
        // this method will get the chunk that needs to be fixed and
        // the value in the map contains the following information:
        // <filenameChunkId, <host1(valid chunk), host2(replica destination), ...>>
        Map<String, List<String>> map = new HashMap<>();
        System.out.println("looking for corrupted chunks...");
        for(String filename : dataMap.keySet()){
            TreeMap treeMap = dataMap.get(filename);
            for(Object id : treeMap.keySet()){
                int chunkId = (int)id;
                List<String> hosts = (List<String>) treeMap.get(chunkId);
                System.out.println("checking for file " + filename + "'s chunk " + chunkId);
                System.out.println("chunk replica number: " + hosts.size());
                if(hosts.size() < Math.min(active.size(), 3)){
                    System.out.println("file " + filename + "'s chunk " + chunkId + " has not enough replica");
                    int i = random.nextInt(hosts.size());
                    String askedHost = hosts.get(i);
                    List<String> fixList = new ArrayList<>();
                    System.out.println("add normal node to the list ");
                    List<String> temp = new ArrayList<>(active);
                    fixList.add(askedHost);
                    for(String host : hosts){
                        temp.remove(host);
                    }
                    int count = Math.min(active.size(), 3) - hosts.size();
                    for(int j = 0; j <= count - 1; j++){
                        fixList.add(temp.get(j));
                        System.out.println("node " + temp.get(j) + " will ask data from node " + askedHost);
                    }
//                    while(fixList.size() != (Math.min(active.size(), 3) - hosts.size())){
//                        System.out.println("replica is not enough and looking for more active node to store");
//                        i = random.nextInt(active.size());
//                        String askingHost = active.get(i);
//                        if(!fixList.contains(askingHost) && !hosts.contains(askingHost)){
//                            fixList.add(askingHost);
//                        }
//                        System.out.println("total number of active node: " + active.size());
//                        System.out.println("current fixList size: " + fixList.size());
//                        if(fixList.size() == (Math.min(3, active.size()) - hosts.size())){
//                            break;
//                        }
//                    }
                    System.out.println("finished for finding node to store replica");
                    String filenameChunkId = filename + " " + chunkId;
                    System.out.println("The file: " + filename + "'s chunkId " + chunkId + " needs to be fixed");
                    map.put(filenameChunkId, fixList);
                }
            }
        }
        return map;
    }

    public synchronized void removeFromDataMap(String filename, int chunkId, String host){
        TreeMap<Integer, List<String>> treeMap = dataMap.get(filename);
        if(treeMap != null){
            List<String> hosts = treeMap.get(chunkId);
            hosts.remove(host);
        }
        else{
            System.out.println("We do not have this file");
        }
    }

    public synchronized void removeDownNodeInfo(List<String> downNodes){
        for(String filename : dataMap.keySet()){
            TreeMap treeMap = dataMap.get(filename);
            for(Object id : treeMap.keySet()){
                int chunkId = (int)id;
                List<String> list = (List<String>) treeMap.get(chunkId);
                List<String> temp = new ArrayList<>(list);
                for(String node : list){
                    if(downNodes.contains(node)){
                        temp.remove(node);
                        System.out.println("file " + filename + "'s chunk " + chunkId + " has lost one replica");
                    }
                }
                treeMap.put(chunkId, temp);
            }
        }
    }

}
