import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class CommitManager {

    private String currentUser;

    Folder rootFolder;

    private String currentRootSha1;
    private String newRootSha1 = null;

    private Map<String, String> currentStateOfFiles = new HashMap<>();
    private Map<String, String> newStateOfFiles = new HashMap<>();

    private Commit currentCommit = null;
    private List<Commit> commitList = new LinkedList<>();

    private List<String> newFiles = new ArrayList<>();
    private List<String> deletedFiles = new ArrayList<>();
    private List<String> updatedFiles = new ArrayList<>();



    CommitManager(){
        rootFolder = getRootFolder();
        rootFolder.updateStateAndSetSha1();
        currentRootSha1 = rootFolder.currentSHA1;
    }

    Commit getMasterCommit(){
        return new Commit("", rootFolder.currentSHA1, rootFolder, null);
    }

    void setCurrentUser(String currentUser){
        this.currentUser = currentUser;
    }

    Commit commit(String msg){
        rootFolder.updateStateAndSetSha1();

        if(haveChanges()){

            currentRootSha1 = newRootSha1;
            newRootSha1 = null;

            Commit com = new Commit(msg, currentRootSha1, rootFolder, currentCommit);
            currentCommit = com;

            rootFolder.commit(currentUser, com.commitTime);
            com.zipCommit();
            commitList.add(com);

            return com;
        }
        return null;
    }


    private Map<String, List<String>> getChanges() {
        /*check for new and deleted files
        // newStateOfFiles.keySet() - currentStateOfFiles.keySet() return the files that exist in newStateOfFiles but not in currentStateOfFiles
        currentStateOfFiles.keySet() - newStateOfFiles.keySet()) return the files that exist in currentStateOfFiles but not in newStateOfFiles*/
        newFiles = new ArrayList<>(CollectionUtils.subtract(newStateOfFiles.keySet(), currentStateOfFiles.keySet()));
        deletedFiles = new ArrayList<>(CollectionUtils.subtract(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));

        //check for updated files
        List<String> common = new ArrayList<>(CollectionUtils.retainAll(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        for(String key : common){
            if (!currentStateOfFiles.get(key).equals(newStateOfFiles.get(key))){
                updatedFiles.add(key);
            }
        }
        Map<String, List<String>> changes = new HashMap<>();
        changes.put("update", updatedFiles);
        changes.put("new", newFiles);
        changes.put("delete", deletedFiles);
        return changes;
    }

    boolean haveChanges(){
        newRootSha1 = rootFolder.currentSHA1;
        return !currentRootSha1.equals(newRootSha1);
    }

    Map<String ,List<String>> getWorkingCopy(){
        calcNewSha1();
        return getChanges();
    }

    private String calcNewSha1(){
        newStateOfFiles = new HashMap<>();
        return rootFolder.updateStateAndSetSha1();
    }

    private Folder getRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

}
