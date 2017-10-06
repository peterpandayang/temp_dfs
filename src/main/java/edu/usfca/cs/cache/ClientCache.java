package edu.usfca.cs.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bingkunyang on 10/1/17.
 */
public class ClientCache {

    public static String RETRIEVE_TEMP_PATH = "retrieveTemp/";
    public static String RETRIEVE_PATH = "retrieve/";
    private static String firstCheckSum = "";
    private static ConcurrentHashMap<String, String> checkSumMap = new ConcurrentHashMap<>();

    public void setFirstCheckSum(String s){
        this.firstCheckSum = s;
    }

    public String getFirstCheckSum(){
        return firstCheckSum;
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

}
