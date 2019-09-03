package core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Blob extends Item {
    final private String typeItem = "File";
    protected Common.FilesStatus state;

    Blob(){}

    Blob(String fullPath){
        this.fullPath = fullPath;
        this.name = new File(fullPath).getName();
    }

    Blob(File itemPath, ItemSha1 sha1, String lastUser, String lastModified, boolean rewriteFS){
        this.fullPath = itemPath.getAbsolutePath();
        this.name = itemPath.getName();
        this.currentSHA1 = sha1;
        this.userLastModified = lastUser;
        this.lastModified = lastModified;

        if (rewriteFS)
            Utils.unzip(Settings.objectsFolderPath + this.currentSHA1 + ".zip",
                    itemPath.getParent(), itemPath.getName());

    }

    String getUser(){ return userLastModified; }
    String getModifiedTime(){ return lastModified; }

    public Common.FilesStatus getState(){ return state; }
    void setState(Common.FilesStatus state){ this.state = state; }

    @Override
    public void zip(){
        updateState();
        if(!isExistInObjects()){
            Utils.zip(getZipPath(), fullPath);
        }
    }

    @Override
    public void updateState(){
        try {
            String content = new String(Files.readAllBytes(Paths.get(fullPath)));
            currentSHA1 = new ItemSha1(content, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getContent(){
        return currentSHA1.getContent();
    }

    String getTypeItem(){ return this.typeItem; }

    @Override
    public String toString(){ return fullPath; }

}
