import java.io.File;
import java.util.Arrays;
import java.util.Comparator;


abstract public class Item {
//TODO change class params to private and add appropriate methods

    String name;
    String currentSHA1;
    String userLastModified;
    String lastModified;
    String fullPath;


    abstract String getTypeItem();
    abstract public void updateState();
    abstract public void zipAndCopy();

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
                currentSHA1 + Settings.delimiter +
                userLastModified + Settings.delimiter +
                lastModified;
    }

    public String getCurrentSHA1(){
        return currentSHA1;
    }
}