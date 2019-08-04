import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.*;

class Branch {

    private Commit startCommit;
    private Commit head;
    private String name;
    private Folder rootFolder;

    private Map<String, Commit> commitData = new HashMap<>();

    private Map<String, String> currentStateOfFiles = new HashMap<>();
    private Map<String, String> newStateOfFiles = new HashMap<>();

    Branch(String name){
        // constractor for master branch
        this.name = name;

        rootFolder = createRootFolder();
        rootFolder.updateState();
        commit("", true);
    }

    Branch(String name, Commit head, Folder rootFolder){
        // constractor for new branch
        this.name = name;
        this.rootFolder = rootFolder;

        setHead(head);
        currentStateOfFiles = rootFolder.getCommittedItemsState();
    }

    private Branch(String name, String headCommitSha1, String startCommitSha1){
        // constractor for loading an existing branch

        this.name = name;

        commitData = Commit.loadAll(startCommitSha1, headCommitSha1);
        this.head = commitData.get(headCommitSha1);
        this.startCommit = commitData.get(startCommitSha1);

        clearCurrentWC();
        File  rootFolderPath = new File(Settings.repositoryFullPath);
        rootFolder = new Folder(rootFolderPath,
                this.head.getRootFolderSHA1(), head.getUserLastModified(),
                head.getCommitTime());

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

    static Branch load(String branchName){
        // TODO deprecate load function and build constractor that knows how to handle only branch name

        List<String> branchData = Utils.getFileLines(getBranchFilePath(branchName));
        String[] branchCommits = branchData.get(0).split(Settings.delimiter);

        String headCommitSha1 = branchCommits[0];
        String startCommitSha1 = branchCommits[1];

        return new Branch(branchName, headCommitSha1, startCommitSha1);
    }

    boolean haveChanges(){
        rootFolder.updateState();
        return !head.getRootFolderSHA1().equals(rootFolder.currentSHA1);
    }

    boolean commit(String msg, boolean force){
        if(head == null || haveChanges() || force){

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
        if (startCommit == null) startCommit = head;

        addCommitToHistory(head);
        writeBranchInfoFile();
    }

    private void writeBranchInfoFile(){
        String branchFileContent =  head.getCommitSHA1()+ Settings.delimiter + startCommit.getCommitSHA1();
        Utils.writeFile(getBranchFilePath(name), branchFileContent, false);
    }

    private static String getBranchFilePath(String branchName){
        return Settings.branchFolderPath + branchName + ".txt";
    }

    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != startCommit){
            res.add(currentCommit.toString());
            currentCommit = commitData.get(currentCommit.getPreviousCommitSHA1());
        }
        return res;
    }

    List<String> getCommittedState(){  return rootFolder.getItemsData(); }

//    public void open() {
//        clearCurrentWC();
//        recursiveOpenAllFiles(getRootSha1(), Settings.repositoryFullPath);
//    }
//

    private void clearCurrentWC() {
        File directory = new File(Settings.repositoryFullPath);
        File[] listOfItems = directory.listFiles();
        for(File item: listOfItems){
            if(item.isDirectory()){
                if(!item.getName().equals(Settings.gitFolder)){
                    deleteSubFilesRec(item);
                }
            }
            else{
                Utils.deleteFile(item.getPath());
            }
        }
    }

    private void deleteSubFilesRec(File folder) {
        File directory = new File(folder.getPath());
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory()) {
                deleteSubFilesRec(item);
            }
            else{
                Utils.deleteFile(item.getPath());
            }
        }
        Utils.deleteFile(folder.getPath());
    }

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

}
