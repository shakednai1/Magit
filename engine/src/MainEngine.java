import java.util.*;

public class MainEngine {

    private RepositoryManager repositoryManager;

    public MainEngine(){
        // TODO - remove before submit
        String fullPath = "C:\\test";
        Settings.setNewRepository(fullPath);
        repositoryManager = new RepositoryManager();
        repositoryManager.switchActiveRepository(fullPath);
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

    public boolean createNewBranch(String name){
        try {
            repositoryManager.getActiveRepository().createNewBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public List<String> getAllBranches(){
        List<String> allBrnachesName = new ArrayList<>();
        List<Branch> allBranches = repositoryManager.getActiveRepository().getAllBranches();
        Branch headBranch = repositoryManager.getActiveRepository().getActiveBranch();
        for(Branch branch:allBranches){
            if (branch.getName().equals(headBranch.getName())){
                allBrnachesName.add("**HEAD** " + branch.getName());
            }
            else {
                allBrnachesName.add(branch.getName());
            }
        }
        return allBrnachesName;
    }

    public boolean deleteBranch(String name){
        try {
            repositoryManager.getActiveRepository().deleteBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public void checkoutBranch(String name){
       repositoryManager.getActiveRepository().checkoutBranch(name);
    }

    public boolean haveOpenChanges(){
        return repositoryManager.getActiveRepository().haveOpenChanges();
    }

    public List<String> getActiveBranchHistory(){
        return repositoryManager.getActiveRepository().getActiveBranch().getCommitHistory();
    }

    public String getCurrentRepoLocation() {
        return repositoryManager.getActiveRepository().getFullPath();
    }

    public boolean validBranchName(String branchName){
        return repositoryManager.validBranchName(branchName);
    }

    public String isXmlValid(String xmlPath){
        XmlLoader xmlLoader = new XmlLoader(xmlPath);
        try {
            xmlLoader.checkValidXml();
            return null;
        } catch (XmlException e) {
            return e.message;
        }
    }

}
