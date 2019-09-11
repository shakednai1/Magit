package core;

import exceptions.NoChangesToCommitError;
import models.BranchData;
import org.apache.commons.collections4.CollectionUtils;
import sun.text.normalizer.CharacterIteratorWrapper;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Branch {

    private Commit head = null;
    private String name;
    private Folder rootFolder;

    String trackingAfter = "null";

    private Map<String, Commit> commitData = new HashMap<>();

    public Map<String, Blob> currentStateOfFiles = new HashMap<>();
    public Map<String, Blob> newStateOfFiles = new HashMap<>();

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
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);
    }

     Branch(String name, String headCommitSha1, String trackingAfter, boolean rewriteFS){
        // constractor for loading an existing branch
        this.name = name;
        this.trackingAfter = trackingAfter;

        commitData = Commit.loadAll(headCommitSha1);
        setHead(commitData.get(headCommitSha1));

        File  rootFolderPath = new File(Settings.repositoryFullPath);
        rootFolder = new Folder(rootFolderPath,
                new ItemSha1(this.head.getRootFolderSHA1(), false, false),
                head.getUserLastModified(),
                head.getCommitTime(),
                rewriteFS);

        currentStateOfFiles = rootFolder.getCommittedFilesState(false);
    }

    Branch(String name, String trackingAfter){
        String branchData = Utils.getFileLines(Settings.remoteBranchesPath + trackingAfter + ".txt").get(0);
        this.name=  name;
        this.trackingAfter = trackingAfter;
        this.head = new Commit(branchData.split(Settings.delimiter)[0]);

        writeBranchInfoFile();
    }

    public String getTrackingAfter(){ return trackingAfter; }

    private void addCommitToHistory(Commit commit){
        commitData.put(commit.getSha1(), commit);
    }

    private Folder createRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        File path = new File(repoLocation);
        return new Folder(path);
    }

    public Commit getHead(){ return head; }

    public String getName(){ return name; }

    Folder getRootFolder(){ return rootFolder; }

    static Branch load(String branchName, boolean rewriteWC){
        // TODO deprecate load function and build constractor that knows how to handle only branch name

        List<String> branchData = Utils.getFileLines(getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0).split(Settings.delimiter)[0];
        String trackingAfterRemote = branchData.get(0).split(Settings.delimiter)[1];

        if(headCommitSha1.equals("null")){
            // loading empty repo
            if (rewriteWC) Utils.clearCurrentWC();
            return new Branch(branchName);
        }

        return new Branch(branchName, headCommitSha1, trackingAfterRemote, rewriteWC);
    }

    static BranchData getBranchDisplayData(String branchName, Repository repository){
        List<String> branchData = Utils.getFileLines(getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0).split(Settings.delimiter)[0];
        String trackingAfter = branchData.get(0).split(Settings.delimiter)[1];
        String headCommitMsg;

        if(headCommitSha1.equals("null")){
            headCommitSha1 = "";
            headCommitMsg = "";
        }
        else{
            Commit headCommit = new Commit(headCommitSha1);
            headCommitSha1 = headCommit.getSha1();
            headCommitMsg = headCommit.getMsg();
        }

        return new BranchData(branchName, headCommitSha1, headCommitMsg, trackingAfter);
    }


    public boolean haveChanges(){
        rootFolder.updateState();
        if (head == null){
            return !rootFolder.isEmptyCurrentState();
        }
        else{
            return !head.getRootFolderSHA1().equals(rootFolder.getSha1());
        }
    }

    Commit commit(String msg) throws NoChangesToCommitError{
        if (!haveChanges())
            throw new NoChangesToCommitError("");

        String commitTime = Settings.commitDateFormat.format(new Date());
        rootFolder.commit(Settings.getUser(), commitTime);

        String prevCommitSha1 = (head==null)? null : head.getSha1();
        Commit com = new Commit(msg, rootFolder.getSha1(), rootFolder.userLastModified, commitTime,prevCommitSha1);
        com.zipCommit();

        setHead(com);
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);

        return com;
    }


    private void updateChangedFilesState(){ // TODO better name for function
        newStateOfFiles.clear();
        newStateOfFiles = rootFolder.getCurrentFilesState(false);
    }

    private void setHead(Commit newHead) {
        head = newHead;

        addCommitToHistory(head);
        writeBranchInfoFile();
    }

    public void writeBranchInfoFile(){
        String branchFileContentCommit =  (head == null)? "null": head.getSha1();
        String branchFileContentTracking = (trackingAfter == null) ? "null" : trackingAfter;
        String content = String.join(Settings.delimiter, branchFileContentCommit, branchFileContentTracking);
        Utils.writeFile(getBranchFilePath(name), content, false);
    }

    private static String getBranchFilePath(String branchName){
        return Settings.branchFolderPath + branchName + ".txt";
    }

    public List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != null){
            res.add(currentCommit.toString());
            currentCommit = commitData.get(currentCommit.getFirstPreviousCommitSHA1());
        }
        return res;
    }

    public List<String> getCommittedState(){  return rootFolder.getItemsData(); }

    FilesDelta getWorkingCopy(){
        rootFolder.updateState();
        updateChangedFilesState();
        return getFilesChanges();
    }

    private FilesDelta getFilesChanges() {
        List<String> newFilesPaths = new ArrayList<>(CollectionUtils.subtract(newStateOfFiles.keySet(), currentStateOfFiles.keySet()));
        List<String> deletedFilesPaths = new ArrayList<>(CollectionUtils.subtract(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        List<String> updatedFilesPaths = new ArrayList<>();

        //check for updated files
        List<String> common = new ArrayList<>(CollectionUtils.retainAll(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        for(String key : common){
            if (!currentStateOfFiles.get(key).equals(newStateOfFiles.get(key))){
                updatedFilesPaths.add(key);
            }
        }

        FilesDelta changes = new FilesDelta();
        changes.setUpdatedFiles(updatedFilesPaths.stream().map(newStateOfFiles::get).collect(Collectors.toList()));
        changes.setNewFiles(newFilesPaths.stream().map(newStateOfFiles::get).collect(Collectors.toList()));
        changes.setDeletedFiles(deletedFilesPaths.stream().map(currentStateOfFiles::get).collect(Collectors.toList()));

        return changes;
    }

    static boolean deleteBranch(String branchName){
        return Utils.deleteFile(getBranchFilePath(branchName));
    }

    public boolean isTracking(){
        return trackingAfter != null;
    }

    public void addTracking(String remoteBranchName){
        trackingAfter = remoteBranchName;
        writeBranchInfoFile();
    }
}
