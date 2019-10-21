package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Blob extends Item {
    final private String typeItem = Common.ItemTypes.File.name();
    protected Common.FilesStatus state;

    Blob(){}

    Blob(String fullPath, Settings repoSettings){
        this.fullPath = fullPath;
        this.name = new File(fullPath).getName();
        this.repoSettings = repoSettings;
    }

    Blob(File itemPath, ItemSha1 sha1, String lastUser, String lastModified, Settings repoSettings){
        this.fullPath = itemPath.getAbsolutePath();
        this.name = itemPath.getName();
        this.currentSHA1 = sha1;
        this.userLastModified = lastUser;
        this.lastModified = lastModified;
        this.repoSettings = repoSettings;
    }

    String getUser(){ return userLastModified; }
    String getModifiedTime(){ return lastModified; }

    public Common.FilesStatus getState(){ return state; }
    void setState(Common.FilesStatus state){ this.state = state; }

    @Override
    public void zip(){
        updateState();
        if(!isExistInObjects()){
            FSUtils.zip(getZipPath(), fullPath);
        }
    }

    @Override
    public void updateState(){
        try {
            String content = new String(Files.readAllBytes(Paths.get(fullPath)));
            currentSHA1 = new ItemSha1(content, true, false, repoSettings.getRepositoryObjectsFullPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getContent(){
        return currentSHA1.getContent();
    }

    void rewriteFS(){
       FSUtils.unzip(repoSettings.objectsFolderPath + this.currentSHA1 + ".zip",
                new File(fullPath).getParent(), name);

    }

    String getTypeItem(){ return this.typeItem; }

    @Override
    public String toString(){ return fullPath; }

}
