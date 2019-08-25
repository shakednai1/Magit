import exceptions.*;
import models.BranchData;
import javax.rmi.CORBA.Util;
import java.io.File;
import java.util.*;

public class MainEngine {

    private static RepositoryManager repositoryManager;
    private static XmlLoader xmlLoader;

    public MainEngine(){
        repositoryManager = new RepositoryManager();
    }

    public static RepositoryManager getRepositoryManager(){
        return repositoryManager;
    }
    public Map<String ,List<String>> getWorkingCopyStatus() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        return repositoryManager.getActiveRepository().getWorkingCopy();
    }

    public boolean commit(String msg) throws NoActiveRepositoryError{
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

    public void createNewBranch(String name, boolean checkout)
            throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError{
        validateActiveRepository();

        repositoryManager.getActiveRepository().createNewBranch(name, checkout);
    }

    public List<BranchData> getAllBranches() throws NoActiveRepositoryError{
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

    public String getCurrentBranchName(){
        return repositoryManager.getActiveRepository().getActiveBranch().getName();
    }

    public Map<String, Commit> getAllCommits(){ return repositoryManager.getActiveRepository().getAllCommits();}

    public void resetBranch(String commitSha1){
        Utils.clearCurrentWC();
        String branchName = getCurrentBranchName();
        Branch branch = new Branch(branchName, commitSha1, true);
        repositoryManager.getActiveRepository().setActiveBranch(branch);
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

    public void fetchRepo(){
        if(repositoryManager.getActiveRepository().hasRemoteRepo()){
            repositoryManager.getActiveRepository().fetch();
        }
        else{
            throw new IllegalArgumentException("current repo has no remote repository");
        }
    }
}
