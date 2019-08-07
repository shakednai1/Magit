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

    public List<String> getCurrentCommitState()
            throws NoActiveRepositoryError, UncommittedChangesError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        if(repositoryManager.getActiveRepository().getActiveBranch().getHead() == null){
            throw new UncommittedChangesError("No active commit");
        }
        return repositoryManager.getActiveRepository().getActiveBranch().getCommittedState();
    }

    public void createNewBranch(String name, boolean checkout)
            throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        repositoryManager.getActiveRepository().createNewBranch(name, checkout);
    }

    public List<String> getAllBranches() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        List<String> allBranchesName = repositoryManager.getActiveRepository().getAllBranches();
        String headBranch = repositoryManager.getActiveRepository().getActiveBranch().getName();
        List<String> res = new ArrayList<>();

        for(String branch: allBranchesName){
            String branchDisplay = (branch.equals(headBranch))? "**HEAD** " + branch : branch;
            res.add(branchDisplay);
        }

        return res;
    }

    public void deleteBranch(String name) throws InvalidBranchNameError, IllegalArgumentException, NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        repositoryManager.getActiveRepository().deleteBranch(name);
    }

    public void checkoutBranch(String name, boolean force) throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError {
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
       repositoryManager.getActiveRepository().checkoutBranch(name, force);
    }

    public List<String> getActiveBranchHistory() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
        return repositoryManager.getActiveRepository().getActiveBranch().getCommitHistory();
    }

    public String getCurrentRepoName() throws NoActiveRepositoryError{
        if(repositoryManager.getActiveRepository() == null){
            throw new NoActiveRepositoryError("No active repository yet");
        }
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
