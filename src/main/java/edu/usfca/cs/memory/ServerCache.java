package edu.usfca.cs.memory;

import edu.usfca.cs.entity.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bingkunyang on 9/20/17.
 */
public class ServerCache extends Cache{
    public static final String[] storageNode = {"bass01", "bass02", "bass03", "bass04", "bass05", "bass06", "bass07", "bass08", "bass09", "bass10"};
    public List<String> activeNode;
    private static String SERVER_HOST = "bass10";
    Map<String, List<Chunk>> map;  // helpful when retrieving file

    public ServerCache(){
        map = new HashMap<>();
        activeNode = new ArrayList<>();
    }


}
