package edu.usfca.cs.dfs;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by bingkunyang on 10/6/17.
 */
public class StoringFileTest {

    public static void main(String[] args) throws IOException {
        String oneFile = "../../../../home2/byang14/bigdata/testfile";
        File file = new File(oneFile);
        Path filePath = Paths.get(file.getPath());
        if(Files.exists(filePath)){
            Files.createDirectories(filePath);
        }
        String content = "This is just a test";
        Files.write(filePath, content.getBytes(), StandardOpenOption.APPEND);
    }

}
