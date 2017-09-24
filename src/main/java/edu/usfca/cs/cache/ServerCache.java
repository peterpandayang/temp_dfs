package edu.usfca.cs.cache;

import edu.usfca.cs.dfs.StorageMessages;

import java.util.*;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ServerCache {

    private Map<String, TreeMap<Integer, List<String>>> dataMap;  // helpful when retrieving file
    private Map<String, Integer> nodeHostMap;
    private List<String> active;
    Random random = new Random();

    public ServerCache(){
        dataMap = new HashMap<>();
        nodeHostMap = new HashMap<>();
        active = new ArrayList<>();
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

    public List<String> getAvailableNodeName() {
        List<String> randomNode = new ArrayList<>();
        List<Integer> randomNodePort = new ArrayList<>();
        while(true){
            int index = random.nextInt(active.size());
            String node = active.get(index);
            int port = nodeHostMap.get(node);
            if(!randomNode.contains(node)){
                randomNode.add(node);
                randomNodePort.add(port);
            }
            if(randomNode.size() == GeneralCache.DEFAULT_REPLICAS){
                break;
            }
        }
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add(randomNode.get(0) + " " + randomNodePort.get(0));
        nodeNames.add(randomNode.get(1) + " " + randomNodePort.get(1));
        nodeNames.add(randomNode.get(2) + " " + randomNodePort.get(2));
        return nodeNames;
    }

}
