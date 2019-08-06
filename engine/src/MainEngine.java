import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;

import java.util.*;

public class MainEngine {

    private RepositoryManager repositoryManager;

    public MainEngine(){
        // TODO - remove before submit
        String fullPath = "C:\\test";
        Settings.setNewRepository(fullPath);
        repositoryManager = new RepositoryManager();
        repositoryManager.switchActiveRepository(fullPath);
//        repositoryManager.createNewRepository(fullPath);
    }

    public Map<String ,List<String>> getWorkingCopyStatus(){
        return repositoryManager.getActiveRepository().getWorkingCopy();
    }

    public boolean commit(String msg){
        boolean force = false; // TODO add functionality for 'true' option
        return repositoryManager.getActiveRepository().getActiveBranch().commit(msg, force);
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

    public List<String> getCurrentCommitState(){
        return repositoryManager.getActiveRepository().getActiveBranch().getCommittedState();
    }

    public void createNewBranch(String name, boolean checkout) throws InvalidBranchNameError, UncommittedChangesError{
        repositoryManager.getActiveRepository().createNewBranch(name, checkout);
    }

    public List<String> getAllBranches(){
        List<String> allBranchesName = repositoryManager.getActiveRepository().getAllBranches();
        String headBranch = repositoryManager.getActiveRepository().getActiveBranch().getName();
        List<String> res = new ArrayList<>();

        for(String branch: allBranchesName){
            String branchDisplay = (branch.equals(headBranch))? "**HEAD** " + branch : branch;
            res.add(branchDisplay);
        }

        return res;
    }

    public void deleteBranch(String name) throws InvalidBranchNameError, IllegalArgumentException{
        repositoryManager.getActiveRepository().deleteBranch(name);
    }

    public void checkoutBranch(String name, boolean force) throws InvalidBranchNameError, UncommittedChangesError {
       repositoryManager.getActiveRepository().checkoutBranch(name, force);
    }

    public List<String> getActiveBranchHistory(){
        return repositoryManager.getActiveRepository().getActiveBranch().getCommitHistory();
    }

    public String getCurrentRepoLocation() {
        return repositoryManager.getActiveRepository().getFullPath();
    }

    public String isXmlValid(String xmlPath){
        XmlLoader xmlLoader = new XmlLoader(xmlPath);
        try {
            xmlLoader.checkValidXml();
            xmlLoader.loadRepo();
            return null;
        } catch (XmlException e) {
            return e.message;
        }
    }

    public void createNewRepository(String newRepositoryPath){
        repositoryManager.createNewRepository(newRepositoryPath, false);
    }

}
