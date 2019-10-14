package core;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


abstract public class Item implements Zipable {
//TODO change class params to private and add appropriate methods

    ItemSha1 currentSHA1;
    String userLastModified;
    String lastModified;
    String fullPath;
    String name;

    Settings repoSettings;

    abstract String getTypeItem();
    abstract public void updateState();

    @Override
    public String getSha1(){
        return currentSHA1.sha1;
    }


    boolean isExistInObjects() { // TODO get the specific file we look for
        File directory = new File(repoSettings.objectsFolderPath);
        File[] listOfItems = directory.listFiles();
        return Arrays.stream(listOfItems).anyMatch(f -> f.getName().equals(currentSHA1 + ".zip"));
    }

    String getZipPath() {
        return repoSettings.objectsFolderPath + currentSHA1 + ".zip";
    }

    void updateUserAndDate(String user, String commitTime) {
        userLastModified = user;
        lastModified = commitTime;
    }

    Comparator<Item> compareToOther = (Item itemA, Item itemB) -> itemA.fullPath.compareTo(itemB.fullPath);

    String getDataString(){
        return fullPath + Settings.delimiter +
                getTypeItem() + Settings.delimiter +
                currentSHA1.sha1 + Settings.delimiter +
                userLastModified + Settings.delimiter +
                lastModified;
    }

    public String getFullPath() { return fullPath; }

    @Override
    public boolean equals(Object object){
        if (object == null) return false;
        if (object.getClass() != this.getClass()) return false;

        Item other = (Item) object;
        if (!fullPath.equals(other.fullPath)) return false;
        if (currentSHA1 == null && other.currentSHA1 == null) return true;
        if (currentSHA1 == null || other.currentSHA1 == null) return false;

        return currentSHA1.equals(other.currentSHA1) ;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}