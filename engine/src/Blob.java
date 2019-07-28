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
    public String updateStateAndSetSha1(){
        try {
            String contents = new String(Files.readAllBytes(Paths.get(fullPath)));
            currentSHA1 = DigestUtils.sha1Hex(contents);
            return currentSHA1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void zipAndCopy(){
        updateStateAndSetSha1();
        boolean exist = isExistInObjects();
        if(!exist){
            Utils.zip(getZipPath(), fullPath);
        }
    }

    public void deepCopy(Blob newBlob){
        newBlob.fullPath = fullPath;
        newBlob.name = name;
        newBlob.path = path;
        newBlob.currentSHA1 = currentSHA1;
        newBlob.typeItem = typeItem;
        newBlob.lastModified = lastModified;
        newBlob.userLastModified = userLastModified;
    }

}
