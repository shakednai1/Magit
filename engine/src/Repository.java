import java.util.List;
import java.util.Map;

class Repository {

    private String fullPath;
    private BranchManager branchManager;

    Repository(String fullPath){
        this.fullPath = fullPath;
        branchManager = new BranchManager();
        branchManager.createNewBranch("master");
    }

    String getFullPath(){ return fullPath; }

    Branch getActiveBranch(){ return branchManager.getActiveBranch(); }

    List<Branch> getAllBranches(){ return branchManager.getAllBranches(); }

    BranchManager getBranchManager(){ return branchManager; } // TODO hide them

    Map<String ,List<String>> getWorkingCopy(){
        return branchManager.getActiveBranch().getCommitManager().getWorkingCopy();
    }

    void checkoutBranch(String name){
        branchManager.checkoutBranch(name);
        saveRepositoryActiveBranch();
    }

    void createNewBranch(String name){
        branchManager.createNewBranch(name);
        saveRepositoryActiveBranch();
    }

    private void saveRepositoryActiveBranch(){
        Utils.writeFile(Settings.activeBranchFilePath,
                branchManager.getActiveBranch().getName(),
                false);
    }


    public boolean validBranchName(String branchName) {
        for(Branch branch: branchManager.getAllBranches()){
            if(branch.getName().equals(branchName)){
                return true;
            }
        }
        return false;
    }
}
