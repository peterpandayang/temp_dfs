package edu.usfca.cs.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeCache {

    public static String PATH = "datanode";
    private Map<String, List<Integer>> dataMap;
    public static final String pathPrefix = GeneralCache.pathPrefix;

    public DataNodeCache(){
        dataMap = new ConcurrentHashMap<>();
    }

    public List<String> getFilenameAndChunkId(){
        List<String> rst = new ArrayList<>();
        if(dataMap.size() == 0){
            return rst;
        }
        for(String filename : dataMap.keySet()){
            List<Integer> chunkIds = dataMap.get(filename);
            for(Integer id : chunkIds){
                String temp = filename + " " + id;
                rst.add(temp);
            }
        }
        dataMap.clear();
        return rst;
    }

    public void updateFileInfo(String filename, int chunkId){
        if(!dataMap.containsKey(filename)){
            dataMap.put(filename, new ArrayList());
        }
        dataMap.get(filename).add(chunkId);

        // should write to the local disk as well...

    }

}
