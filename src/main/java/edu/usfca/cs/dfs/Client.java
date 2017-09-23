package edu.usfca.cs.dfs;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import edu.usfca.cs.handler.ClientSocketHandler;
import edu.usfca.cs.memory.ClientCache;


public class Client {

    private static ClientCache cache;
//    private static ClinetFileHandler fileHandler;
    private static ClientSocketHandler socketHandler;

    public Client() {
        try {
            cache = new ClientCache(getHostname());
            socketHandler = new ClientSocketHandler();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the short host name of the current host.
     *
     * @return name of the current host
     */
    private static String getHostname() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostName();
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.start();
    }

    private void start() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while (true) {
            System.out.println("please enter your command...");
            line = scanner.nextLine();
            if (line.equals("EOF")) {
                break;
            }
            if (cache.parse(line)) {
                String method = line.split(" ")[0].toLowerCase();
                if(method.equals("post")){
                    makePost(line.split(" ")[1].toLowerCase());
                }
                else{

                }
            }
        }
    }

    private void makePost(String filename) throws IOException {
        // check if the file exist
        File file = new File(cache.FILE_PATH + filename);
        if(file.exists()){
            int kilobytes = (int) (file.length() / 1024);  // limit is 4295 GB for the use of integer
            if(kilobytes < 1024){
                socketHandler.clientReqToServer(cache, filename, 0);
            }
            else{
                int loop = kilobytes % 1024 == 0 ? (kilobytes / 1024) : (kilobytes / 1024) + 1;
                for(int i = 1; i <= loop ; i++){
                    socketHandler.clientReqToServer(cache, filename, i - 1);
                }
            }
        }
        else{
            System.out.println("The file you want to store does not exists");
        }
    }

}
