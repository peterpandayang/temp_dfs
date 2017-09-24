package edu.usfca.cs.memory;

/**
 * Created by bingkunyang on 9/20/17.
 */
public class ClientCache extends Cache{
    public String hostname;
    public String input;
    public static final int DEFAULT_CHUNK_SIZE = 1024 * 1024;   // limit for a chunk is 1MB
    public static String FILE_PATH = "temp/";
//    public static String SERVER_HOST = "bass10";
    public static String SERVER_HOST = "localhost";

    public ClientCache(String hostname){
        this.hostname = hostname;
    }

    public boolean parse(String line) {
        if(line == null || line.length() == 0){
            System.out.println("should enter something");
            return false;
        }
        String[] temp = line.split(" ");
        if(temp.length != 2){
            System.out.println("Invalid number of parameters, please enter again");
            return false;
        }
        String method = temp[0].toLowerCase();
        if(!method.equals("get") && !method.equals("post")){
            System.out.println("Please follow the pattern: [<get/post>, <filename.txt>]");
            return false;
        }
        return true;
    }
}
