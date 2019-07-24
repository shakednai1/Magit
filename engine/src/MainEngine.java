import java.util.*;

public class MainEngine {

    public static String currentUser = "Admin";

    public MainEngine(){
        Repository repository = new Repository("C:\\test");
        RepositoryManager.switchActiveRepository(repository.getFullPath());
        RepositoryManager.getAllRepositories().add(repository);
        RepositoryManager.getActiveRepository().getCommitManager().start();
    }

    public static Map<String ,List<String>> getWorkingCopyStatus(){
        return RepositoryManager.getActiveRepository().getCommitManager().getWorkingCopy();
    }

    public static boolean commit(String msg){
        return RepositoryManager.getActiveRepository().getCommitManager().commit(msg);
    }

    public static void chnageCurrentUser(String user){
        currentUser = user;
    }

    public static boolean changeActiveRepository(String fullPath){
        try {
            RepositoryManager.switchActiveRepository(fullPath);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public static List<String> getCurrentCommitState(){
        return RepositoryManager.getActiveRepository().getCommitManager().currentCommit.getAllItems();
    }

    public static boolean createNewBranch(String name){
        try {
            RepositoryManager.getActiveRepository().getBranchManager().createNewBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public static List<String> getAllBranches(){
        List<String> allBrnachesName = new ArrayList<>();
        List<Branch> allBranches = RepositoryManager.getActiveRepository().getBranchManager().getAllBranches();
        Branch headBranch = RepositoryManager.getActiveRepository().getBranchManager().getActiveBranch();
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

    public static boolean deleteBranch(String name){
        try {
            RepositoryManager.getActiveRepository().getBranchManager().deleteBranch(name);
            return true;
        }
        catch (IllegalArgumentException e){
            return false;
        }
    }

    public static List<String> getActiveBrancHistory(){
        return RepositoryManager.getActiveRepository().getBranchManager().getActiveBranch().getCommitHistory();
    }

    public static String getCurrentRepoLocation() {
        return RepositoryManager.getActiveRepository().getFullPath();
    }
}
