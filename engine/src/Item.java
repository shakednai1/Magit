import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;


abstract public class Item {
//TODO change class params to private and add appropriate methods

    String typeItem; // TODO enum
    String name;
    String currentSHA1;
    String userLastModified;
    String lastModified;
    String fullPath;

    abstract public String updateStateAndSetSha1();

    abstract public void zipAndCopy();

    boolean isExistInObjects() {
        File directory = new File(Settings.objectsFolderPath);
        File[] listOfItems = directory.listFiles();
        return Arrays.stream(listOfItems).anyMatch(f -> f.getName().equals(currentSHA1 + ".zip"));
    }

    String getZipPath() {
        return RepositoryManager.getActiveRepository().getObjectsFolderPath() + currentSHA1 + ".zip";
    }

    void updateUserAndDate(String user, String commitTime) {
        userLastModified = user;
        lastModified = commitTime;
    }

    Comparator<Item> compareToOther = (Item itemA, Item itemB) -> itemA.fullPath.compareTo(itemB.fullPath);

    String getDataString(){
        return fullPath + Settings.delimiter + typeItem +
                Settings.delimiter + currentSHA1 + Settings.delimiter + userLastModified +
                Settings.delimiter + lastModified;
    }

}