import java.io.FileWriter;
import java.io.IOException;

class Repository {

    private String fullPath;
    private BranchManager branchManager;
    private CommitManager commitManager;

    Repository(String fullPath){
        this.fullPath = fullPath;
        commitManager = new CommitManager();
        branchManager = new BranchManager(commitManager);
        branchManager.createAndSetMasterBranch();
    }

    String getFullPath(){ return fullPath; }

    BranchManager getBranchManager(){ return branchManager; }

    CommitManager getCommitManager(){ return commitManager; }

    void saveRepositoryActiveBranch(){
        try{
            FileWriter fileWriter = new FileWriter(Settings.activeBranchFilePath, false);
            fileWriter.write(branchManager.getActiveBranch().getName());
            fileWriter.close();
        }
        catch (IOException e){/*Lets assume all good*/}
    }

    boolean checkoutBranch(String name){
        boolean force = false; // TODO give user to determine if force or not
        branchManager.checkoutBranch(force, name);
        saveRepositoryActiveBranch();
        return true; //TODO return false when needed
    }
}
