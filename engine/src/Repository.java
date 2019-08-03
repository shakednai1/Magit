import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Repository {

    private String fullPath;
    private Branch activeBranch;
    private List<Branch> branches = new ArrayList<>(); // TODO convert to list of names


    Repository(String fullPath){
        this.fullPath = fullPath;

        // TODO check if load or create
        createNewBranch("master");
    }

    String getFullPath(){ return fullPath; }

    Branch getActiveBranch(){ return activeBranch; }

    List<Branch> getAllBranches(){ return branches; }

    Map<String ,List<String>> getWorkingCopy(){
        return activeBranch.getWorkingCopy();
    }

    void createNewBranch(String branchName){
        for(Branch branch: branches){
            if (branch.getName().equals(branchName)){
                throw new IllegalArgumentException();
            }
        }

        Commit newBranchHead = (activeBranch == null)? null :activeBranch.getHead();
        Branch newBranch = new Branch(branchName, newBranchHead);
        activeBranch = newBranch;
        saveRepositoryActiveBranch();

        branches.add(newBranch);
    }

    void checkoutBranch(String name){
        for(Branch branch:branches){
            if(branch.getName().equals(name)){
                activeBranch = branch;
                branch.open();
                break;
            }
        }
        saveRepositoryActiveBranch();
    }

    void deleteBranch(String branchName){
        if(activeBranch.getName().equals(branchName)){
            throw new IllegalArgumentException();
        }
        for(Branch branch: branches){
            if(branch.getName().equals(branchName)){
                Branch.deleteBranch(branchName);
                branches.remove(branch);
            }
        }
    }

    boolean haveOpenChanges(){ return activeBranch.haveChanges();}

    private void saveRepositoryActiveBranch(){
        Utils.writeFile(Settings.activeBranchFilePath, activeBranch.getName(), false);
    }


    public boolean validBranchName(String branchName) {
        for(Branch branch: branches){
            if(branch.getName().equals(branchName)){
                return true;
            }
        }
        return false;
    }
}
