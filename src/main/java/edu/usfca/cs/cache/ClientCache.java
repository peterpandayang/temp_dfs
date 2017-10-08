package edu.usfca.cs.cache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingkunyang on 10/1/17.
 */
public class ClientCache {

    public static String RETRIEVE_TEMP_PATH = "client/retrieveTemp/";
    public static String RETRIEVE_PATH = "client/retrieve/";
    private static String firstCheckSum = "";
    private static ConcurrentHashMap<String, String> checkSumMap = new ConcurrentHashMap<>();
    private static Set<String> storedFile = new HashSet<>();

    public void setFirstCheckSum(String s){
        this.firstCheckSum = s;
    }

    public String getFileCheckSum(String filename){
        return checkSumMap.get(filename);
    }

    public void addToCheckSumMap(String filename, String firstCheckSum){
        checkSumMap.put(filename, firstCheckSum);
    }

    public boolean checkInCheckSumMap(String filename, String checksum){
        if(checkSumMap.containsKey(filename)){
            if(checksum.equals(checkSumMap.get(filename))){
                return true;
            }
            else{
                System.out.println("the checksum does not match");
                return false;
            }
        }
        System.out.println("you do not have this file");
        return false;
    }

    public ClientCache(){}

    public boolean addToFileSet(String filename){
        return storedFile.add(filename);
    }

    public void clearAllFile(){
        storedFile = new HashSet<>();
    }

    public boolean checkIfExits(String filename){
        return storedFile.contains(filename);
    }

}
