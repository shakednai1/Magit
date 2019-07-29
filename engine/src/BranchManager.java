import java.util.ArrayList;
import java.util.List;

public class BranchManager {


    private CommitManager commitManager;
    private Branch activeBranch;
    private List<Branch> branches = new ArrayList<>();

    BranchManager(CommitManager commitManager){ this.commitManager=commitManager;}

    List<Branch> getAllBranches(){ return branches; }

    Branch getActiveBranch(){ return activeBranch; }

    private Commit getActiveBranchHead(){ return activeBranch.getHead(); }

    void setActiveBranch(Branch branch){ activeBranch = branch; }

    void createNewBranch(String branchName){
        for(Branch branch: branches){
            if (branch.getName().equals(branchName)){
                throw new IllegalArgumentException();
            }
        }
        Branch newBranch = new Branch(branchName, getActiveBranchHead());
        setActiveBranch(newBranch);
        branches.add(newBranch);
        Utils.createNewFile(Settings.branchFolderPath + branchName + ".txt",
                newBranch.getHead().commitSha1 + Settings.delimiter + newBranch.getStartCommit().commitSha1);
    }

    Branch createMasterBranch(){
        Commit masterCommit = commitManager.getMasterCommit();
        Branch branch =  new Branch("master", masterCommit);
        Utils.createNewFile(Settings.branchFolderPath + "master" + ".txt",
                masterCommit.commitSha1 + Settings.delimiter + masterCommit.commitSha1);
        return branch;
    }

    void deleteBranch(String branchName){
        if(getActiveBranch().getName().equals(branchName)){
            throw new IllegalArgumentException();
        }
        for(Branch branch: branches){
            if(branch.getName().equals(branchName)){
                branches.remove(branch);
            }
        }
    }

    void addBranch(Branch branch){
        branches.add(branch);
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

    boolean commit(String msg){
        Commit newCommit = commitManager.commit(msg, false);
        if (newCommit == null){
            return false;
        }
        activeBranch.setHead(newCommit);
        return true;
    }

}
