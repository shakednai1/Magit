import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class CommitManager {

    private Folder rootFolder;
    private Commit currentCommit = null;

    private Map<String, String> currentStateOfFiles = new HashMap<>();
    private Map<String, String> newStateOfFiles = new HashMap<>();

    private List<String> newFiles = new ArrayList<>();
    private List<String> deletedFiles = new ArrayList<>();
    private List<String> updatedFiles = new ArrayList<>();

    CommitManager(){
        rootFolder = getRootFolder();
    }

    Commit commit(String msg, boolean force){
        rootFolder.updateState();

        if(haveChanges() || force){
            Commit com = new Commit(msg, rootFolder.currentSHA1, rootFolder, currentCommit);
            currentCommit = com;

            rootFolder.commit(Settings.getUser(), com.commitTime);
            com.zipCommit();

            currentStateOfFiles = newStateOfFiles;

            return com;
        }

        // TODO commit need to update current state at commit manager
//        for (Blob file : rootFolder.curSubFiles.values()) {
//            currentStateOfFiles.put(file.fullPath, file.currentSHA1);
//        }
//
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
        return currentCommit == null || !currentCommit.commitSha1.equals(rootFolder.currentSHA1);
    }

    Map<String ,List<String>> getWorkingCopy(){
        updateFilesState();
        return getChanges();
    }

    private void updateFilesState(){ // TODO refactor
        newStateOfFiles = new HashMap<>();
        rootFolder.updateState();
        for (Blob file : rootFolder.curSubFiles.values()){
            newStateOfFiles.put(file.fullPath, file.currentSHA1);
        }
    }

    private Folder getRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

    public List<String> getLastChanges(){
        return rootFolder.getItemsData();
    }

}
