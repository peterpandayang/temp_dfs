package edu.usfca.cs.handler;

import edu.usfca.cs.cache.ClientCache;
import edu.usfca.cs.io.FileIO;
import edu.usfca.cs.route.ClientFileRetriever;
import edu.usfca.cs.route.ClientFileSender;
import edu.usfca.cs.route.ClientRemoveCmdSender;
import edu.usfca.cs.route.StorageRequester;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
        System.out.println("FYI: The chunk size in this system is 1MB");
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
                    if(!cache.checkIfExits(filename)){
                        System.out.println("The file you're retrieving does not exist");
                        continue;
                    }
                    initThreadPool();
                    ClientFileRetriever retriever = new ClientFileRetriever(filename, myHostname, threadPool);
                    retriever.startGetReq();
                    closeThreadPool();
                }
                else if(method.equals("put")){
                    // to be handled...
                }
                else if(method.equals("rm")){
                    // do the remove here...
                    if(filename.equals("-rf")) {
                        cache.clearAllFile();
                        ClientRemoveCmdSender removeCmdSender = new ClientRemoveCmdSender(filename);
                        removeCmdSender.startRemoveAll();
                    }
                    else{
                        // maybe remove a specific node or a file
                    }
                }
                else if(method.equals("ask")){
                    System.out.println("sending ask storage request");
                    StorageRequester requester = new StorageRequester(cache);
                    if(filename.trim().equals("storage")){
                        requester.requestStorage();
                    }
                }
                else if(line.trim().equals("ls -l")){
                    List<String> files = cache.getAllFilename();
                    if(files.size() == 0){
                        System.out.println("No file in the system");
                        continue;
                    }
                    for(String file : files){
                        System.out.println(file);
                    }
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
        if(!method.equals("get") && !method.equals("post") && !method.equals("rm") && !method.equals("ls") && !method.equals("ask")){
            System.out.println("Please follow the pattern: [<get/post>, <filename.txt>] or [rm -rf] or [ls -l]");
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
            boolean hasFile = cache.checkIfExits(filename);
            if(hasFile){
                System.out.println("This file has already in the system, please store another file");
                return;
            }
            cache.addToFileSet(filename);
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
                String checkSum = io.getCheckSum(dataString);
                cache.addToCheckSumMap(filename, checkSum);
                System.out.println("merge fail");
            }
        }
        else{
            System.out.println("The file you want to store does not exists");
        }
    }

}
