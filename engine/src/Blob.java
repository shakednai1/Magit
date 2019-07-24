import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Blob extends Item {

    public String path;

    public Blob(String path, String name){
        typeItem = "File";
        fullPath = path;
        this.name = name;
        this.path = fullPath.substring(0, fullPath.lastIndexOf("/") +1);
    }

    @Override
    public String calculateSha1(){
        try {
            String contents = new String(Files.readAllBytes(Paths.get(fullPath)));
            sha1 = DigestUtils.sha1Hex(contents);
            return sha1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void zipAndCopy(){
        calculateSha1();
        boolean exist = isExistInObjects();
        if(!exist){
            Utils.zip(getZipPath(), fullPath);
        }
    }

    public void deepCopy(Blob newBlob){
        newBlob.fullPath = fullPath;
        newBlob.name = name;
        newBlob.path = path;
        newBlob.sha1 = sha1;
        newBlob.typeItem = typeItem;
        newBlob.lastModified = lastModified;
        newBlob.userLastModified = userLastModified;
    }

}
