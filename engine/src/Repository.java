
public class Repository {

    private String fullPath;
    private BranchManager branchManager;
    private CommitManager commitManager;
    private String objectsFolderPath;
    private String branchesFolderPath;


    Repository(String fullPath){
        this.fullPath = fullPath;
        objectsFolderPath = fullPath + Settings.objectsFolderPath;
        branchesFolderPath = fullPath + Settings.branchFolderPath;

        commitManager = new CommitManager();
        branchManager = new BranchManager(commitManager);

        Branch masterBranch = branchManager.createMasterBranch();
        branchManager.addBranch(masterBranch);
        branchManager.setActiveBranch(masterBranch);
    }

    String getFullPath(){ return fullPath; }

    BranchManager getBranchManager(){ return branchManager; }

    CommitManager getCommitManager(){ return commitManager; }

    String getObjectsFolderPath(){ return objectsFolderPath; }

    String getBranchesFolderPath(){ return branchesFolderPath; }

    public Branch getActiveBranch(){
        return branchManager.getActiveBranch();
    }

    void setUser(String user){
        commitManager.setCurrentUser(user);
    }

}
