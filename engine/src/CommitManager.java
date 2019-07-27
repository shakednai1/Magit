import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommitManager {

    public Folder mf;

    public String currentRootSha1 = null;
    public String newRootSha1 = null;

    public Map<String, String> currentState = new HashMap<>();
    public Map<String, String> newState = new HashMap<>();

    public Commit currentCommit = null;
    public List<Commit> commitList = new LinkedList<>();

    public List<String> newFiles = new ArrayList<>();
    public List<String> deletedFiles = new ArrayList<>();
    public List<String> updatedFiles = new ArrayList<>();



    public CommitManager(){
    }

    public void start(){
        mf = getRootFolder();
        currentRootSha1 = mf.calculateSha1();
        currentState.putAll(newState);
        newState.clear();
        Commit com = new Commit("", currentRootSha1, mf, null);
        currentCommit = com;
        RepositoryManager.getActiveRepository().getBranchManager().getActiveBranch().setHead(currentCommit);
        mf.addAllItemsToCurentCommit();
        commitList.add(com);
    }


    public boolean commit(String msg){
        if(haveChanges()){
            currentState.clear();
            currentState.putAll(newState);
            newState.clear();

            currentRootSha1 = newRootSha1;
            newRootSha1 = null;

            Commit com = new Commit(msg, currentRootSha1, mf, currentCommit);
            currentCommit = com;
            RepositoryManager.getActiveRepository().getBranchManager().getActiveBranch().setHead(currentCommit);
            mf.zipAll();
            mf.addAllItemsToCurentCommit();
            com.createCommit();
            commitList.add(com);

            return true;
        }
        return false;
    }


    private Map<String, List<String>> getChanges() {
        /*check for new and deleted files
        // newState.keySet() - currentState.keySet() return the files that exist in newState but not in currentState
        currentState.keySet() - newState.keySet()) return the files that exist in currentState but not in newState*/
        newFiles = new ArrayList<>(CollectionUtils.subtract(newState.keySet(), currentState.keySet()));
        deletedFiles = new ArrayList<>(CollectionUtils.subtract(currentState.keySet(), newState.keySet()));

        //check for updated files
        List<String> common = new ArrayList<>(CollectionUtils.retainAll(currentState.keySet(), newState.keySet()));
        for(String key : common){
            if (!currentState.get(key).equals(newState.get(key))){
                updatedFiles.add(key);
            }
        }
        Map<String, List<String>> changes = new HashMap<>();
        changes.put("update", updatedFiles);
        changes.put("new", newFiles);
        changes.put("delete", deletedFiles);
        return changes;
    }

    public boolean haveChanges(){
        newRootSha1 = calcNewSha1();
        return !currentRootSha1.equals(newRootSha1);
    }

    public Map<String ,List<String>> getWorkingCopy(){
        calcNewSha1();
        return getChanges();
    }

    private String calcNewSha1(){
        newState = new HashMap<>();
        return mf.calculateSha1();
    }

    private Folder getRootFolder(){
        String repoLocation = RepositoryManager.getActiveRepository().getFullPath();
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

}
