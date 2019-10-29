package core;

import exceptions.NoChangesToCommitError;
import models.BranchData;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Branch {

    private Settings repoSettings;

    private Commit head = null;
    private String name;
    private Folder rootFolder;

    String trackingAfter = "null";

    private Map<String, Commit> commitData = new HashMap<>();

    public Map<String, Blob> currentStateOfFiles = new HashMap<>();
    public Map<String, Blob> newStateOfFiles = new HashMap<>();

    Branch(String name, Settings repoSettings){
        // constractor for master branch
        this.name = name;
        this.repoSettings = repoSettings;

        rootFolder = createRootFolder(repoSettings.repositoryFullPath);
        rootFolder.updateState();
        writeBranchInfoFile();
    }

    Branch(String name, Commit head, Folder rootFolder, Settings repoSettings){
        // constractor for new branch
        this.name = name;
        this.rootFolder = rootFolder;
        this.repoSettings = repoSettings;

        setHead(head);
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);
    }

     Branch(String name, String headCommitSha1, String trackingAfter, boolean rewriteFS, Settings repoSettings){
        // constractor for loading an existing branch
        this.name = name;
        this.trackingAfter = trackingAfter;
        this.repoSettings = repoSettings;

        commitData = Commit.loadAll(headCommitSha1, repoSettings);
        setHead(commitData.get(headCommitSha1));

        File  rootFolderPath = new File(repoSettings.repositoryFullPath);
        rootFolder = new Folder(rootFolderPath,
                new ItemSha1(this.head.getRootFolderSHA1(), false, false, repoSettings.getRepositoryObjectsFullPath()),
                head.getUserLastModified(),
                head.getCommitTime(),
                repoSettings);
        if(rewriteFS)
            rootFolder.rewriteFS();

        currentStateOfFiles = rootFolder.getCommittedFilesState(false);
    }

    Branch(String name, String trackingAfter, Settings repoSettings){
        String branchData = FSUtils.getFileLines(repoSettings.remoteBranchesPath + trackingAfter + ".txt").get(0);
        this.name=  name;
        this.trackingAfter = trackingAfter;
        this.head = new Commit(branchData.split(Settings.delimiter)[0], repoSettings);
        this.repoSettings = repoSettings;

        writeBranchInfoFile();
    }

    public String getTrackingAfter(){ return trackingAfter; }

    protected Settings getRepoSettings(){ return repoSettings;}

    private void addCommitToHistory(Commit commit){
        commitData.put(commit.getSha1(), commit);
    }

    private Folder createRootFolder(String repoLocation){
        File path = new File(repoLocation);
        return new Folder(path, repoSettings);
    }

    public Commit getHead(){ return head; }

    public String getName(){ return name; }

    Folder getRootFolder(){ return rootFolder; }

    public static Branch load(Settings repoSettings, String branchName, boolean rewriteWC){
        // TODO deprecate load function and build constractor that knows how to handle only branch name

        List<String> branchData = FSUtils.getFileLines(repoSettings.getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0).split(Settings.delimiter)[0];
        String trackingAfterRemote = branchData.get(0).split(Settings.delimiter)[1];

        if(headCommitSha1.equals("null")){
            // loading empty repo
            if (rewriteWC) FSUtils.clearWC(repoSettings.repositoryFullPath);
            return new Branch(branchName, repoSettings);
        }

        return new Branch(branchName, headCommitSha1, trackingAfterRemote, rewriteWC, repoSettings);
    }

    static BranchData getBranchDisplayData(String branchName, Repository repository){
        List<String> branchData = FSUtils.getFileLines(repository.getSettings().getBranchFilePath(branchName));
        String headCommitSha1 = branchData.get(0).split(Settings.delimiter)[0];
        String trackingAfter = branchData.get(0).split(Settings.delimiter)[1];
        String headCommitMsg;

        if(headCommitSha1.equals("null")){
            headCommitSha1 = "";
            headCommitMsg = "";
        }
        else{
            Commit headCommit = new Commit(headCommitSha1, repository.getSettings());
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

    Commit commit(String msg, String secondCommit) throws NoChangesToCommitError{
        if (!haveChanges())
            throw new NoChangesToCommitError();

        String commitTime = Settings.commitDateFormat.format(new Date());
        rootFolder.commit(repoSettings.getUser(), commitTime);

        String prevCommitSha1 = (head==null)? null : head.getSha1();
        Commit com = new Commit(msg, rootFolder.getSha1(),
                repoSettings.getUser(), commitTime,
                prevCommitSha1, secondCommit,
                repoSettings);
        com.zipCommit();

        setHead(com);
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);

        return com;
    }

    void mergeCommit(Merge merge, boolean rewriteFolder){
        merge.folderChanges.commit(repoSettings.getUser(), merge.mergeTime);

        if (rewriteFolder)
            rewriteFolderByMerge(merge);
    }

    void rewriteFolderByMerge(Merge merge){
        rootFolder = merge.folderChanges.getResFolder();
        rootFolder.rewriteFS();
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);
    }


    private void updateChangedFilesState(){ // TODO better name for function
        newStateOfFiles.clear();
        newStateOfFiles = rootFolder.getCurrentFilesState(false);
    }

    protected void setRootFolder(Folder folder, boolean rewriteFS){
        rootFolder = folder;
        currentStateOfFiles = rootFolder.getCommittedFilesState(false);

        if(rewriteFS)
            rootFolder.rewriteFS();
    }

    protected void setHead(Commit newHead) {
        head = newHead;

        addCommitToHistory(head);
        writeBranchInfoFile();
    }

    public void writeBranchInfoFile(){
        String branchFileContentCommit =  (head == null)? "null": head.getSha1();
        String branchFileContentTracking = (trackingAfter == null) ? "null" : trackingAfter;
        String content = String.join(Settings.delimiter, branchFileContentCommit, branchFileContentTracking);
        FSUtils.writeFile(repoSettings.getBranchFilePath(name), content, false);
    }

    public List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != null){
            res.add(currentCommit.toString());
            currentCommit = commitData.get(currentCommit.getFirstPrecedingSha1());
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

    public boolean isTracking(){
        return trackingAfter != null && !trackingAfter.equals("null");
    }

    public void addTracking(String remoteBranchName){
        trackingAfter = remoteBranchName;
        writeBranchInfoFile();
    }
}
