package edu.usfca.cs.memory;

import edu.usfca.cs.dfs.StorageMessages;
import edu.usfca.cs.entity.Chunk;

import java.util.*;

/**
 * Created by bingkunyang on 9/20/17.
 */
public class ServerCache extends Cache{
    public static final String[] storageNode = {"bass01", "bass02", "bass03", "bass04", "bass05", "bass06", "bass07", "bass08", "bass09", "bass10"};
    public static List<String> activeNode = Arrays.asList(storageNode); // assuming that all good at beginning and could modify later
    private static String SERVER_HOST = "bass10";
    public Map<String, TreeMap<Integer, List<String>>> map;  // helpful when retrieving file
    Random random = new Random();
    int DEFAULT_REPLICAS = 3;

    public ServerCache(){
        map = new HashMap<>();
    }

    public void initialize(StorageMessages.StoreChunk storeChunkMsg){
        String filename = storeChunkMsg.getFileName();
        int chunkId = storeChunkMsg.getChunkId();
        if(!map.containsKey(filename)){
            map.put(filename, new TreeMap<Integer, List<String>>());
        }
        TreeMap<Integer, List<String>> idMap = map.get(filename);
        if(!idMap.containsKey(chunkId)){
            idMap.put(chunkId, new ArrayList<String>());
        }
    }

    public List<String> getAvailableNodeName() {
        List<String> randomNode = new ArrayList<>();
        List<Integer> randomNodePort = new ArrayList<>();
        while(true){
            int index = random.nextInt(activeNode.size());
            String nodeName = activeNode.get(index);
            int port = CLIENT_STORAGE_PORTS[index];
            if(!randomNode.contains(nodeName)){
                randomNode.add(nodeName);
                randomNodePort.add(port);
            }
            if(randomNode.size() == DEFAULT_REPLICAS){
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
