package edu.usfca.cs.thread;

import edu.usfca.cs.route.ClientFileRetriever;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bingkunyang on 10/1/17.
 */
public class ClientRetrieveFileThread extends Thread{

    private ClientFileRetriever retriever;
    private String filename;
    private String chunkIdHost;

    public ClientRetrieveFileThread(ClientFileRetriever retriever, String filename, String chunkIdHost){
        this.retriever = retriever;
        this.filename = filename;
        this.chunkIdHost = chunkIdHost;
    }

    @Override
    public void run() {
        try {
            retriever.retrieveOneChunkAndStore(filename, chunkIdHost);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
