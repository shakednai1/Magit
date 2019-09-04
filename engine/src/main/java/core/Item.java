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


    abstract String getTypeItem();
    abstract public void updateState();

    @Override
    public String getSha1(){
        return currentSHA1.sha1;
    }


    boolean isExistInObjects() { // TODO get the specific file we look for
        File directory = new File(Settings.objectsFolderPath);
        File[] listOfItems = directory.listFiles();
        return Arrays.stream(listOfItems).anyMatch(f -> f.getName().equals(currentSHA1 + ".zip"));
    }

    String getZipPath() {
        return Settings.objectsFolderPath + currentSHA1 + ".zip";
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
        return currentSHA1.equals(other.currentSHA1) && fullPath.equals(other.fullPath);
    }

    public String getName(){
        return name;
    }
}