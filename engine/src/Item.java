import java.io.File;
import java.util.Arrays;

abstract public class Item {
//TODO change class params to private and add appropriate methods

    public String typeItem;
    public String name;
    public String sha1;
    public String userLastModified;
    public String lastModified;
    public String fullPath;

    abstract public String calculateSha1();
    abstract public void zipAndCopy();

    public boolean isExistInObjects(){
        File directory = new File(RepositoryManager.getActiveRepository().getObjectsFolderPath());
        File[] listOfItems = directory.listFiles();
        return Arrays.stream(listOfItems).anyMatch(f -> f.getName().equals(sha1 + ".zip"));
    }

    public String getZipPath(){
        return RepositoryManager.getActiveRepository().getObjectsFolderPath() + sha1 + ".zip";
    }

    public void updateUserAndDate(){
        userLastModified = MainEngine.currentUser;
        lastModified = RepositoryManager.getActiveRepository().getCommitManager().currentCommit.commitTime;
    }


}