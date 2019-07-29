import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Blob extends Item {

    private String path;

    Blob(String path, String name){
        typeItem = "File";
        fullPath = path;
        this.name = name;
        this.path = fullPath.substring(0, fullPath.lastIndexOf("/") +1);
    }

    @Override
    public void zipAndCopy(){
        updateState();
        if(!isExistInObjects()){
            Utils.zip(getZipPath(), fullPath);
        }
    }

    @Override
    public void updateState(){
        try {
            String contents = new String(Files.readAllBytes(Paths.get(fullPath)));
            currentSHA1 = DigestUtils.sha1Hex(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
