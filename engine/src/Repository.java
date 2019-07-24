
public class Repository {

    private String fullPath;
    private BranchManager branchManager;
    private CommitManager commitManager;
    private String objectsFolderPath;
    private String branchesFolderPath;


    public Repository(String fullPath){
        this.fullPath = fullPath;
        objectsFolderPath = fullPath + "/.magit/.objects/";
        branchesFolderPath = fullPath + "/.magit/.branches/";
        branchManager =new BranchManager();
        branchManager.createMasterBranch();
        commitManager = new CommitManager();
    }

    public String getFullPath(){ return fullPath; }

    public BranchManager getBranchManager(){ return branchManager; }

    public CommitManager getCommitManager(){ return commitManager; }

    public String getObjectsFolderPath(){ return objectsFolderPath; }

    public String getBranchesFolderPath(){ return branchesFolderPath; }

}
