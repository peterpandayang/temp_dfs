package edu.usfca.cs.handler;

import edu.usfca.cs.route.ClientFileRetriever;
import edu.usfca.cs.route.ClientFileSender;
import edu.usfca.cs.thread.ClientPostReqThread;

import java.io.*;
import java.util.Scanner;

/**
 * Created by bingkunyang on 9/24/17.
 */
public class ClientSideHandler {
    private static String myHostname;
    private static String FILE_PATH = "temp/";

    public ClientSideHandler(String myHostname){
        this.myHostname = myHostname;
    }

    public void start() throws IOException {
        startClient();
    }

    /**
     * This is preprocessing part for the usr's input
     * @throws IOException
     */
    private void startClient() throws IOException {
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
                    ClientFileRetriever retriever = new ClientFileRetriever(filename);
                    retriever.startGetReqThread();
                }
                else if(method.equals("put")){

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
    private void splitFileAndSend(String filename) throws IOException {
        // check if the file exist
        File file = new File(FILE_PATH + filename);
        if(file.exists()){
            InputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024];
            BufferedInputStream bin = new BufferedInputStream(inputStream);
            int byteread;
            int chunkId = 0;
            while((byteread = bin.read(buffer)) != -1){
                String data = new String(buffer, 0, byteread);
                ClientFileSender sender = new ClientFileSender(myHostname, filename, chunkId, data);
                sender.startPostReqThread();
                chunkId++;
            }
        }
        else{
            System.out.println("The file you want to store does not exists");
        }
    }

}
