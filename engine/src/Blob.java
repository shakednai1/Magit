import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Blob extends Item {
    final private String typeItem = "File";

    private String path;

    Blob(String path, String name){
        fullPath = path;
        this.name = name;
        this.path = fullPath.substring(0, fullPath.lastIndexOf("/") +1);
    }

    Blob(File itemPath, String sha1, String lastUser, String lastModified ){
        this.fullPath = itemPath.getPath();
        this.name = itemPath.getName();
        this.currentSHA1 = sha1;
        this.userLastModified = lastUser;
        this.lastModified = lastModified;

        Utils.unzip(Settings.objectsFolderPath + this.currentSHA1 + ".zip",
                itemPath.getParent(), this.name );

    }

    Blob(String Path, String name, String content, String lastUser, String lastModified){
        this.fullPath = Path;
        this.name = name;
        this.currentSHA1 = DigestUtils.sha1Hex(content);
        this.userLastModified = lastUser;
        this.lastModified = lastModified;
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

    String getTypeItem(){ return this.typeItem; }


}
