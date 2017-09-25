package edu.usfca.cs.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class DataNodeCache {

    private Map<String, List<Integer>> dataMap;

    public DataNodeCache(){
        dataMap = new HashMap<>();
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
        return rst;
    }

}
