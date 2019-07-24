import java.util.ArrayList;
import java.util.List;

public class BranchManager {

    private Branch activeBranch;
    private List<Branch> branches = new ArrayList<>();


    public BranchManager(){}

    public List<Branch> getAllBranches(){ return branches; }

    public Branch getActiveBranch(){ return activeBranch; }

    private Commit getActiveBranchHead(){ return activeBranch.getHead(); }

    public void setActiveBranch(Branch branch){ activeBranch = branch; }

    public void createNewBranch(String branchName){
        String branchFolderPath = RepositoryManager.getActiveRepository().getBranchesFolderPath();
        for(Branch branch: branches){
            if (branch.getName().equals(branchName)){
                throw new IllegalArgumentException();
            }
        }
        Branch newBranch = new Branch(branchName, getActiveBranchHead());
        setActiveBranch(newBranch);
        branches.add(newBranch);
        Utils.createNewFile(branchFolderPath + branchName + ".txt", newBranch.getHead().commitSha1);
    }

    public void createMasterBranch(){
        Branch master = new Branch("master", null);
        setActiveBranch(master);
        branches.add(master);
    }

    public void deleteBranch(String branchName){
        if(getActiveBranch().getName().equals(branchName)){
            throw new IllegalArgumentException();
        }
        for(Branch branch: branches){
            if(branch.getName().equals(branchName)){
                branches.remove(branch);
            }
        }
    }

    public void checkoutBranch(boolean force, String branchToCheckout){
        CommitManager commitManager = RepositoryManager.getActiveRepository().getCommitManager();
        if(commitManager.haveChanges() && !force){
            throw new UnsupportedOperationException();
        }
        for(Branch branch:branches){
            if(branch.getName().equals(branchToCheckout)){
                setActiveBranch(branch);
                // TODO update HEAD file to the newActiveBranch.getName();
                // TODO open all zip file of the commit
                break;
            }
        }
    }

}
