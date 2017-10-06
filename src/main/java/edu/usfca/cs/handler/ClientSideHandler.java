package edu.usfca.cs.handler;

import edu.usfca.cs.cache.ClientCache;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.route.ClientFileRetriever;
import edu.usfca.cs.route.ClientFileSender;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientSideHandler {
    private static String myHostname;
    private static String FILE_PATH = "temp/";
    private static ThreadPoolExecutor threadPool;
    private ClientCache cache = new ClientCache();
    private FileIO io = new FileIO();

    public ClientSideHandler(String myHostname){
        this.myHostname = myHostname;
    }

    public void start() throws IOException, InterruptedException, NoSuchAlgorithmException {
        startClient();
    }

    public void initThreadPool(){
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    }

    public void closeThreadPool(){
        int activeThreads = threadPool.getActiveCount();
        while(activeThreads != 0){
            activeThreads = threadPool.getActiveCount();
        }
        threadPool.shutdown();
    }

    /**
     * This is preprocessing part for the usr's input
     * @throws IOException
     */
    private void startClient() throws IOException, InterruptedException, NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while (true) {
            System.out.println("please enter your command...");
            line = scanner.nextLine();
            if (line.equals("EOF")) {
                break;
            }
            if (parse(line)) {
                String method = line.split(" ")[0].toLowerCase();
                String filename = line.split(" ")[1].toLowerCase();
                if(method.equals("post")){
                    splitFileAndSend(filename);
                }
                else if (method.equals("get")){
                    // this will do the retrieval in parallel
                    initThreadPool();
                    ClientFileRetriever retriever = new ClientFileRetriever(filename, myHostname, threadPool);
                    retriever.startGetReq();
                    closeThreadPool();
                }
                else if(method.equals("put")){
                    // to be handled...
                }
                else{

                }
            }
        }
    }


    private boolean parse(String line) {
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

    /**
     * send request based on the # of the chunks
     * @param filename
     * @throws IOException
     */
    private void splitFileAndSend(String filename) throws IOException, InterruptedException, NoSuchAlgorithmException {
        // check if the file exist
        File file = new File(FILE_PATH + filename);
        if(file.exists()){
            InputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream bin = new BufferedInputStream(inputStream);
            int byteread;
            int chunkId = 0;
            StringBuilder sb = new StringBuilder();
            while((byteread = bin.read(buffer)) != -1){
                String data = new String(buffer, 0, byteread);
                sb.append(data);
                ClientFileSender sender = new ClientFileSender(myHostname, filename, chunkId, data);
                sender.startPostReq();
                chunkId++;
            }
            String dataString = new String(Files.readAllBytes(Paths.get(file.getPath())));
            if(sb.toString().equals(dataString)){
                System.out.println("merge successful");
                String checkSum = io.getCheckSum(dataString);
                cache.setFirstCheckSum(checkSum);
                System.out.println("initial checksum is: " + checkSum);
                cache.addToCheckSumMap(filename, checkSum);
            }
            else{
                System.out.println("merge fail");
            }
        }
        else{
            System.out.println("The file you want to store does not exists");
        }
    }

}
