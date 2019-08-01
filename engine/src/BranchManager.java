import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.util.ArrayList;
import java.util.List;

class BranchManager {
    // TODO isn't that too much hirarchy?

    private Branch activeBranch;
    private List<Branch> branches = new ArrayList<>();

    BranchManager(){}

    List<Branch> getAllBranches(){ return branches; }

    Branch getActiveBranch(){ return activeBranch; }

    private void setActiveBranch(Branch branch){
        activeBranch = branch;  }

    void createNewBranch(String branchName){
        for(Branch branch: branches){
            if (branch.getName().equals(branchName)){
                throw new IllegalArgumentException();
            }
        }

        Commit newBranchHead = (activeBranch == null)? null :activeBranch.getHead();
        Branch newBranch = new Branch(branchName, newBranchHead);
        setActiveBranch(newBranch);

        branches.add(newBranch);
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

    public boolean haveChangesInActiveBranch(){
        return activeBranch.haveChanges();
    }

    void checkoutBranch(String branchToCheckout){
        for(Branch branch:branches){
            if(branch.getName().equals(branchToCheckout)){
                setActiveBranch(branch);
                branch.open();
                break;
            }
        }
    }

    boolean commit(String msg) {
        return activeBranch.commit(msg, false);
    }

}
