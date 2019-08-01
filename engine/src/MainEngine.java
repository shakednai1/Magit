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
        return repositoryManager.getActiveRepository().getBranchManager().commit(msg);
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
        Branch headBranch = repositoryManager.getActiveRepository().getBranchManager().getActiveBranch();
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
            repositoryManager.getActiveRepository().getBranchManager().deleteBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public boolean checkoutBranch(String name){
        try {
            repositoryManager.getActiveRepository().checkoutBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public List<String> getActiveBrancHistory(){
        return repositoryManager.getActiveRepository().getBranchManager().getActiveBranch().getCommitHistory();
    }

    public String getCurrentRepoLocation() {
        return repositoryManager.getActiveRepository().getFullPath();
    }


}
