package edu.usfca.cs.cache;

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

}
