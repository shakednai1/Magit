package core;

import exceptions.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import models.BranchData;
import models.CommitData;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainEngine {

    private static RepositoryManager repositoryManager = new RepositoryManager();
    private static XmlLoader xmlLoader;

    public MainEngine(){ }

    public static RepositoryManager getRepositoryManager(){
        return repositoryManager;
    }

    public static Repository getActiveRepository(){
        return repositoryManager.getActiveRepository();
    }


    public FilesDelta getWorkingCopyStatus() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        return repositoryManager.getActiveRepository().getWorkingCopy();
    }

    public CommitData commit(String msg) throws NoActiveRepositoryError, NoChangesToCommitError{
        validateActiveRepository();
        return repositoryManager.getActiveRepository().commitActiveBranch(msg);
    }

    public String getUser(){
        return Settings.getUser();
    }

    public void changeCurrentUser(String user){
        Settings.setUser(user);
    }

    public boolean changeActiveRepository(String fullPath){
        try {
            repositoryManager.switchActiveRepository(fullPath);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public List<String> getCurrentCommitState()
            throws NoActiveRepositoryError, UncommittedChangesError{
        validateActiveRepository();

        if(repositoryManager.getActiveRepository().getActiveBranch().getHead() == null){
            throw new UncommittedChangesError("No active commit");
        }
        return repositoryManager.getActiveRepository().getActiveBranch().getCommittedState();
    }

    public BranchData createNewBranch(String name, boolean checkout)
            throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError{
        validateActiveRepository();

        return repositoryManager.getActiveRepository().createNewBranch(name, checkout);
    }

    public ObservableList<BranchData> getAllBranches() throws NoActiveRepositoryError{
        validateActiveRepository();
        return repositoryManager.getActiveRepository().getAllBranches();
    }

    public void deleteBranch(String name) throws InvalidBranchNameError, IllegalArgumentException, NoActiveRepositoryError{
        validateActiveRepository();
        repositoryManager.getActiveRepository().deleteBranch(name);
    }

    private void validateActiveRepository() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
    }

    public void checkoutBranch(String name, boolean force) throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError {
        validateActiveRepository();
        repositoryManager.getActiveRepository().checkoutBranch(name, force);
    }

    public List<String> getActiveBranchHistory() throws NoActiveRepositoryError{
        validateActiveRepository();
        return repositoryManager.getActiveRepository().getActiveBranch().getCommitHistory();
    }

    public String getCurrentRepoName() throws NoActiveRepositoryError{
        validateActiveRepository();
        return repositoryManager.getActiveRepository() != null ? repositoryManager.getActiveRepository().getName() : "";
    }

    public String isXmlValid(String xmlPath) throws XmlException {
        xmlLoader = new XmlLoader(xmlPath);
        xmlLoader.checkValidXml();
        return xmlLoader.checkRepoLocation();
    }

    public void loadRepositoyFromXML() throws UncommittedChangesError, InvalidBranchNameError{
        xmlLoader.loadRepo();
    }


    public void createNewRepository(String newRepositoryPath, String name){
        File directory = new File(newRepositoryPath);
        if(directory.exists()){
            throw new IllegalArgumentException(newRepositoryPath + " is already exist");
        }
        repositoryManager.createNewRepository(newRepositoryPath, name, false);
    }

    public static String getCurrentBranchName(){
        return repositoryManager.getActiveRepository().getActiveBranch().getName();
    }

    public ObservableMap<String, CommitData> getAllCommitsData(){ return repositoryManager.getActiveRepository().getAllCommitsData();}

    public void resetBranch(String commitSha1){
        Utils.clearCurrentWC();

        try{
            repositoryManager.getActiveRepository().resetActiveBranch(commitSha1);
        }
        catch(NoActiveRepositoryError e) {}
    }

    public Branch getActiveBranch() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        return repositoryManager.getActiveRepository().getActiveBranch();
    }

    public String getCurrentRepoPath() throws NoActiveRepositoryError{
        validateActiveRepository();
        return repositoryManager.getActiveRepository() != null ? repositoryManager.getActiveRepository().getFullPath() : "";
    }

    public void cloneRepo(String src, String dst, String repoName){
        repositoryManager.cloneRepository(src, dst, repoName);
    }

    public void fetchRepo() throws NoActiveRepositoryError{
        validateActiveRepository();
        if(repositoryManager.getActiveRepository().hasRemoteRepo()){
            repositoryManager.getActiveRepository().fetch();
        }
        else{
            throw new IllegalArgumentException("current repo has no remote repository");
        }
    }

    public List<String> getAllRemoteBranchesName(){
        String remoteRepoName= repositoryManager.getActiveRepository().getRemoteRepositoryName();
        return repositoryManager.getActiveRepository().getAllRemoteBranches().stream().map((branch) -> remoteRepoName + "/" + branch.getName()).collect(Collectors.toList());
    }

    public void createAndCheckoutToNewTrackingBranch(String newBranchName, String trackingAfter) {
        Branch branch = new Branch(newBranchName, trackingAfter);
        repositoryManager.getActiveRepository().addNewBranch(branch);
    }

    public FolderChanges getDiffBetweenCommits(String commitSha1, String prevCommit){
        CommitsDelta commitsDelta= new CommitsDelta(commitSha1, prevCommit);
        commitsDelta.calcFilesMergeState();
        return commitsDelta.getRootFolderChanges();
    }

    public Folder getFileSystemOfCommit(String commitSha1){
        return Commit.getCommitRootFolder(commitSha1);
    }

    public BranchData createNewBranchFromSha1(String name, String sha1, boolean track){
        String trackingAfter = track ? findTrackingAfterBySha1(sha1): null;
        return repositoryManager.getActiveRepository().createNewBranchFromSha1(name, sha1, trackingAfter);
    }

    public boolean isValidBranchName(String name) throws InvalidBranchNameError, NoActiveRepositoryError {
        validateActiveRepository();
        return repositoryManager.getActiveRepository().isValidBranchName(name);
    }

    public String findTrackingAfterBySha1(String sha1){
        File rbDir = new File(Settings.remoteBranchesPath);
        for (File rb : rbDir.listFiles()){
            if(Utils.getFileLines(rb.getPath()).get(0).split(Settings.delimiter)[0].equals(sha1)){
                return rb.getName().split(".txt")[0];
            }
        }
        return null;
    }

    public String getSha1FromRemoteBranch(String remote){
        return Utils.getFileLines(Settings.remoteBranchesPath + remote + ".txt").get(0).split(Settings.delimiter)[0];
    }

    public List<String> getFileLines(String fileSha1){
        return Utils.getZippedContent(fileSha1);
    }


    public static String getBranchMergeName(){
        return getActiveRepository().getCurrentMerge().getMergingBranch().getName();
    }

    public Merge pull(){
        if(getActiveRepository().getActiveBranch().isTracking()){
            return getActiveRepository().pull();
        }
        else{
            throw new IllegalArgumentException("Current active branch is not remote tracking branch");
        }
    }

    public Merge getCurrentMerge(){
        return getActiveRepository().getCurrentMerge();
    }

    public void push() {
        getActiveRepository().push();
    }
}
