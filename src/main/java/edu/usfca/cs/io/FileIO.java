package edu.usfca.cs.io;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bingkunyang on 10/1/17.
 */
public class FileIO {

    private static Writer writer = null;

    public void writeGeneralFile(File file, String data) throws IOException {
        Path filename = Paths.get(file.getPath());
        if(!Files.exists(filename)){
            Files.createFile(filename);
        }
        else{
            Files.delete(filename);
            Files.createFile(filename);
        }
        Files.write(filename, data.getBytes(), StandardOpenOption.APPEND);

    }

    public boolean fileIsValid(File file, File checkSum) throws IOException, NoSuchAlgorithmException {
        if(file.exists() && checkSum.exists()) {
            System.out.println("do the check...");
            String dataString = new String(Files.readAllBytes(Paths.get(file.getPath())));
            String checkSumString = new String(Files.readAllBytes(Paths.get(checkSum.getPath())));
            String calculatedCheckSum = getMD5Str(dataString);
            boolean rst = calculatedCheckSum.equals(checkSumString);
            if(!rst){
                System.out.println("The file has been corrupted");
            }
            else{
                System.out.println("successful matching...");
            }
            return rst;
        }
        return false;
    }

    /**
     *  Reference: http://www.cnblogs.com/renchunxiao/p/3411370.html
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private static String getMD5Str(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] bytes = md5.digest(str.getBytes());
        String result = "";
        for(byte b : bytes) {
            String temp = Integer.toHexString(b & 0xff);
            if(temp.length() == 1) {
                temp = "0" + temp;
            }
            result = result + temp;
        }
        return result;
    }

    public String getCheckSum(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return getMD5Str(data);
    }

    public String getFileContent(File file) throws IOException {
        if(file.exists()){
            String fileString = new String(Files.readAllBytes(Paths.get(file.getPath())));
            return fileString;
        }
        else{
            return "";
        }
    }

    public void writeLog(String filename, int chunkId, String path) throws IOException {
        File file = new File(path);
        Path filePath = Paths.get(file.getPath());
//        if(!Files.exists(filePath)){
//            Files.createFile(filePath);
//        }
        if(!file.exists()){
            file.createNewFile();
        }
        System.out.println("storing filename is : " + filename);
        String data = filename + " " + chunkId + ",";
        Files.write(filePath, data.getBytes(), StandardOpenOption.APPEND);
    }

    public void removeFilenamChunkId(String filename, int chunkId, String path) throws IOException {
        String curr = filename + " " + chunkId;
        File file = new File(path);
        Path filePath = Paths.get(file.getPath());
        String logInfo = new String(Files.readAllBytes(Paths.get(file.getPath())));
        Files.delete(filePath);
        Files.createFile(filePath);
        String[] infos = logInfo.split(",");
        for(String filenamChunkId : infos){
            if(!filenamChunkId.trim().equals(curr)){
                String data = filenamChunkId + ",";
                Files.write(filePath, data.getBytes(), StandardOpenOption.APPEND);
            }
        }
    }

}
