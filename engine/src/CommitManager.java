import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class CommitManager {

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
        //rootFolder.updateStateAndSetSha1();
        currentRootSha1 = "";
    }

    Commit getMasterCommit(){
        Commit commit = commit("", true);
        for (Blob file : rootFolder.curSubFiles.values()) {
            currentStateOfFiles.put(file.fullPath, file.currentSHA1);
        }
        return commit;
    }

    Commit commit(String msg, boolean force){
        rootFolder.updateStateAndSetSha1();

        if(haveChanges() || force){

            currentRootSha1 = newRootSha1;
            newRootSha1 = null;

            Commit com = new Commit(msg, currentRootSha1, rootFolder, currentCommit);
            currentCommit = com;

            rootFolder.commit(Settings.getUser(), com.commitTime);
            com.zipCommit();
            commitList.add(com);

            currentStateOfFiles = newStateOfFiles;

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
        String sha1 = rootFolder.updateStateAndSetSha1();
        for (Blob file : rootFolder.curSubFiles.values()){
            newStateOfFiles.put(file.fullPath, file.currentSHA1);
        }
        return sha1;
    }

    private Folder getRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

}
