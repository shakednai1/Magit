import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class CommitManager {

    private Folder rootFolder;
    private Commit currentCommit = null;

    private Map<String, String> currentStateOfFiles = new HashMap<>();
    private Map<String, String> newStateOfFiles = new HashMap<>();

    CommitManager(){
        rootFolder = getRootFolder();
    }

    // TODO think of a flow where we do not need that function
    void setCurrentCommit(Commit newCommit){ currentCommit = newCommit; }

    private Folder getRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

    boolean haveChanges(){
        return currentCommit == null || !currentCommit.getSHA1().equals(rootFolder.currentSHA1);
    }

    Commit commit(String msg, boolean force){
        updateState();

        if(haveChanges() || force){
            Commit com = new Commit(msg, rootFolder.currentSHA1, rootFolder, currentCommit);

            rootFolder.commit(Settings.getUser(), com.getCommitTime());
            com.zipCommit();

            currentCommit = com;
            setNewStateToCurrent();

            return com;
        }

        return null;
    }

    private void setNewStateToCurrent(){
        currentStateOfFiles = newStateOfFiles;
        newStateOfFiles = new HashMap<>();
    }

    private void updateState(){ // TODO should be in branch method
        rootFolder.updateState();
        updateChangedFilesState();
    }

    private void updateChangedFilesState(){ // TODO better name for function
        newStateOfFiles.clear();
        newStateOfFiles = rootFolder.getItemsState();
    }

    Map<String ,List<String>> getWorkingCopy(){
        updateState();
        return getFilesChanges();
    }

    private Map<String, List<String>> getFilesChanges() {
        List<String> newFiles = new ArrayList<>(CollectionUtils.subtract(newStateOfFiles.keySet(), currentStateOfFiles.keySet()));
        List<String> deletedFiles = new ArrayList<>(CollectionUtils.subtract(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        List<String> updatedFiles = new ArrayList<>();

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

    List<String> getCommittedItemsData(){
        return rootFolder.getItemsData();
    }

}
