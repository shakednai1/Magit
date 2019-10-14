package core;

import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import exceptions.*;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import models.BranchData;
import models.CommitData;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainEngine {

    private RepositoryManager repositoryManager;
    private XmlLoader xmlLoader;
    private boolean canPull = true;

    public MainEngine(String user){
        repositoryManager  = new RepositoryManager(user);
    }

    public RepositoryManager getRepositoryManager(){
        return repositoryManager;
    }

    public Repository getActiveRepository(){
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
        canPull = false;
        return repositoryManager.getActiveRepository().commitActiveBranch(msg);
    }

    public String getUser(){
        return repositoryManager.getSettings().getUser();
    }

    public void changeCurrentUser(String user){
        repositoryManager.getSettings().setUser(user);
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

    public String isXmlValid(String xml) throws XmlException {
        xmlLoader = new XmlLoader(xml, repositoryManager);
        xmlLoader.checkValidXml();
        return xmlLoader.checkRepoLocation();
    }

    public void loadRepositoryFromXML() throws UncommittedChangesError, InvalidBranchNameError{
        xmlLoader.loadRepo();
    }


    public void createNewRepository(String newRepositoryPath, String name) throws InvalidRepositoryPath{
        File directory = new File(newRepositoryPath);
        if(directory.exists()){
            throw new InvalidRepositoryPath(newRepositoryPath + " is already exist");
        }

        repositoryManager.createNewRepository(newRepositoryPath, name, false);
    }

    public String getCurrentBranchName(){
        return repositoryManager.getActiveRepository().getActiveBranch().getName();
    }

    public ObservableMap<String, CommitData> getAllCommitsData(){ return repositoryManager.getActiveRepository().getAllCommitsData();}

    public void resetBranch(String commitSha1){
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

    public Folder getFileSystemOfCommit(String commitSha1){
        return Commit.getCommitRootFolder(commitSha1, repositoryManager.getSettings());
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
        File rbDir = new File(repositoryManager.getSettings().remoteBranchesPath);
        for (File rb : rbDir.listFiles()){
            if(FSUtils.getFileLines(rb.getPath()).get(0).split(Settings.delimiter)[0].equals(sha1)){
                return rb.getName().split(".txt")[0];
            }
        }
        return null;
    }

    public String getSha1FromRemoteBranch(String remote){
        return FSUtils.getFileLines(repositoryManager.getSettings().remoteBranchesPath + remote + ".txt").get(0).split(Settings.delimiter)[0];
    }

    public List<String> getFileLines(String fileSha1){
        return FSUtils.getZippedContent(repositoryManager.settings.objectsFolderPath, fileSha1);
    }


    public String getBranchMergeName(){
        return getActiveRepository().getCurrentMerge().getMergingBranchName();
    }

    public Merge pull(){
        if(getActiveRepository().isRemote()) {
            return getActiveRepository().pull();
        }
        else{
            throw new IllegalArgumentException("current repo has no remote repository");
        }
    }

    public Merge getCurrentMerge(){
        return getActiveRepository().getCurrentMerge();
    }

    public void push() throws IllegalArgumentException{
        if(getActiveRepository().isRemote()){
            getActiveRepository().push();
            canPull = true;
        }
        else{
            throw new IllegalArgumentException("current repo has no remote repository");
        }
    }

    public boolean getCanPull(){
        return canPull;
    }

    public Repository getActiveRepo(){
        return repositoryManager.getActiveRepository();
    }
}
