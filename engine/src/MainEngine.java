import exceptions.InvalidBranchNameError;
import exceptions.NoActiveBranchError;
import exceptions.NoActiveRepositoryError;
import exceptions.UncommittedChangesError;

import java.util.*;

public class MainEngine {

    private static RepositoryManager repositoryManager;

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

    public boolean commit(String msg){
        boolean force = false; // TODO add functionality for 'true' option
        return repositoryManager.getActiveRepository().commitActiveBranch(msg, force);
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

    public List<String> getAllBranches() throws NoActiveRepositoryError{
        validateActiveRepository();

        List<Map<String, String>> allBranches = repositoryManager.getActiveRepository().getAllBranches();
        String headBranch = repositoryManager.getActiveRepository().getActiveBranch().getName();
        List<String> res = new ArrayList<>();

        for(Map<String , String > branch: allBranches){
            String branchDisplay = branch.get("name") + ", "+ branch.get("headSha1") + ", "+ branch.get("headMsg");
            if (branch.get("name").equals(headBranch))
                branchDisplay = "**HEAD** " + branchDisplay;
            res.add(branchDisplay);
        }

        return res;
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

    public String isXmlValid(String xmlPath){
        XmlLoader xmlLoader = new XmlLoader(xmlPath);
        try {
            xmlLoader.checkValidXml();
            xmlLoader.loadRepo();
            return null;
        } catch (XmlException e) {
            return e.message;
        }
        catch (UncommittedChangesError | InvalidBranchNameError e){
            return e.getMessage();
        }
    }


    public void createNewRepository(String newRepositoryPath, String name){
        repositoryManager.createNewRepository(newRepositoryPath, name, false);
    }

}
