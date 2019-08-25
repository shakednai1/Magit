import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Branch {

    private Commit head = null;
    private String name;
    private Folder rootFolder;

    RemoteBranch trackingAfter = null;

    private Map<String, Commit> commitData = new HashMap<>();

    public Map<String, String> currentStateOfFiles = new HashMap<>();
    public Map<String, String> newStateOfFiles = new HashMap<>();

    Branch(String name){
        // constractor for master branch
        this.name = name;

        rootFolder = createRootFolder();
        rootFolder.updateState();
        writeBranchInfoFile();
    }

    Branch(String name, Commit head, Folder rootFolder){
        // constractor for new branch
        this.name = name;
        this.rootFolder = rootFolder;

        setHead(head);
        currentStateOfFiles = rootFolder.getCommittedItemsState();
    }

     Branch(String name, String headCommitSha1, boolean rewriteFS){
        // constractor for loading an existing branch
        this.name = name;

        commitData = Commit.loadAll(headCommitSha1);
        setHead(commitData.get(headCommitSha1));

        File  rootFolderPath = new File(Settings.repositoryFullPath);
        rootFolder = new Folder(rootFolderPath,
                this.head.getRootFolderSHA1(), head.getUserLastModified(),
                head.getCommitTime(),
                rewriteFS);

        currentStateOfFiles = rootFolder.getCommittedItemsState();
    }

    private void addCommitToHistory(Commit commit){
        commitData.put(commit.getCommitSHA1(), commit);
    }

    private Folder createRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

    Commit getHead(){ return head; }

    String getName(){ return name; }

    Folder getRootFolder(){ return rootFolder; }

    static Branch load(String branchName, boolean rewriteWC){
        // TODO deprecate load function and build constractor that knows how to handle only branch name

        List<String> branchData = Utils.getFileLines(getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0);

        if(headCommitSha1.equals("null")){
            // loading empty repo
            if (rewriteWC) Utils.clearCurrentWC();
            return new Branch(branchName);
        }

        return new Branch(branchName, headCommitSha1, rewriteWC);
    }

    static Map<String, String> getBranchDisplayDetails(String branchName){
        List<String> branchData = Utils.getFileLines(getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0);
        String headCommitMsg;
        if(headCommitSha1.equals("null")){
            headCommitSha1 = "";
            headCommitMsg = "";
        }
        else{
            Commit headCommit = new Commit(headCommitSha1);
            headCommitSha1 = headCommit.getCommitSHA1();
            headCommitMsg = headCommit.getMsg();
        }

        return getFormattedBranchDetails(branchName, headCommitSha1, headCommitMsg);
    }

    static Map<String, String> getFormattedBranchDetails(String branchName, String headCommitSha1, String headCommitMsg){
        Map<String , String > res = new HashMap<>();
        res.put("name", branchName);
        res.put("headSha1", headCommitSha1);
        res.put("headMsg", headCommitMsg);

        return res;
    }

    boolean haveChanges(){
        rootFolder.updateState();
        if (head == null){
            return !rootFolder.isEmptyCurrentState();
        }
        else{
            return !head.getRootFolderSHA1().equals(rootFolder.currentSHA1);
        }
    }

    boolean commit(String msg){
        if(haveChanges()){

            String commitTime = Commit.commitDateFormat.format(new Date());
            rootFolder.commit(Settings.getUser(), commitTime);

            String prevCommitSha1 = (head==null)? null : head.getCommitSHA1();
            Commit com = new Commit(msg, rootFolder.currentSHA1, rootFolder.userLastModified, commitTime,prevCommitSha1);
            com.zipCommit();

            setHead(com);
            currentStateOfFiles = rootFolder.getCommittedItemsState();

            return true;
        }

        return false;
    }

    private void updateChangedFilesState(){ // TODO better name for function
        newStateOfFiles.clear();
        newStateOfFiles = rootFolder.getCurrentItemsState();
    }

    private void setHead(Commit newHead) {
        head = newHead;

        addCommitToHistory(head);
        writeBranchInfoFile();
    }

    private void writeBranchInfoFile(){
        String branchFileContent =  (head == null)? "null": head.getCommitSHA1();
        Utils.writeFile(getBranchFilePath(name), branchFileContent, false);
    }

    private static String getBranchFilePath(String branchName){
        return Settings.branchFolderPath + branchName + ".txt";
    }

    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != null){
            res.add(currentCommit.toString());
            currentCommit = commitData.get(currentCommit.getPreviousCommitSHA1());
        }
        return res;
    }

    List<String> getCommittedState(){  return rootFolder.getItemsData(); }

    Map<String ,List<String>> getWorkingCopy(){
        rootFolder.updateState();
        updateChangedFilesState();
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

    static boolean deleteBranch(String branchName){
        return Utils.deleteFile(getBranchFilePath(branchName));
    }

    public boolean isTracking(){
        return trackingAfter != null;
    }

    public void addTracking(RemoteBranch remoteBranch){
        trackingAfter = remoteBranch;
    }
}
