package edu.usfca.cs.cache;

/**
 * Created by bingkunyang on 10/1/17.
 */
public class ClientCache {

    public static String RETRIEVE_TEMP_PATH = "retrieveTemp/";
    public static String RETRIEVE_PATH = "retrieve/";
    private static String firstCheckSum = "";

    public void setFirstCheckSum(String s){
        this.firstCheckSum = s;
    }

    public String getFirstCheckSum(){
        return firstCheckSum;
    }

    public ClientCache(){}

}
