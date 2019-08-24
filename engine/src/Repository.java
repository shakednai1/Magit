import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import models.BranchData;

import java.io.File;
import java.util.*;

class Repository {

    private String name;
    private String fullPath;
    private Branch activeBranch = null;
    private List<BranchData> branches = new LinkedList<>();


    Repository(String fullPath, String name, boolean empty) {
        this.fullPath = fullPath;
        this.name = name;

        saveRepositoryDetails();

        if (!empty) {
            try {
                createNewBranch("master", true);
            } catch (UncommittedChangesError | InvalidBranchNameError e) { /* cant be ?*/ }
        }
    }

    private Repository(String fullPath, Branch activeBranch){
        this.fullPath = fullPath;
        this.name = loadRepositoryName();

        loadBranchesData();

        this.activeBranch = activeBranch;
        saveRepositoryActiveBranch();
    }

    private String loadRepositoryName(){
        List<String> repoDetails = Utils.getFileLines(Settings.repositoryDetailsFilePath);
        return repoDetails.get(0);
    }

    String getFullPath(){ return fullPath; }
    String getName(){ return name; }

    Branch getActiveBranch(){ return activeBranch; }

    List<BranchData> getAllBranches(){ return branches; }

    static Repository load(String repositoryPath){
        // we know that the repo exists and valid

        Settings.setNewRepository(repositoryPath);

        List<String> contentByLines = Utils.getFileLines(Settings.activeBranchFilePath);
        String activeBranchName = contentByLines.get(0);

        Branch activeBranch = Branch.load(activeBranchName, false);
        return new Repository(Settings.repositoryFullPath, activeBranch);
    }

    private void loadBranchesData(){
        branches.clear();

        File directory = new File(Settings.branchFolderPath);
        File[] listOfItems = directory.listFiles();
        for(File item: listOfItems){
            if(!item.getName().equals(Settings.activeBranchFileName)){
                String[] name= item.getName().split("\\.");
                branches.add(Branch.getBranchDisplayData(name[0]));
            }
        }
    }

    boolean  commitActiveBranch(String msg){
        // this function is for assert that branch details at `branches` object will stay updated

        boolean committed = activeBranch.commit(msg);
        if (committed )
            updateActiveBranchDataInHistory();

        return committed;
    }


    private void updateActiveBranchDataInHistory(){
        // TODO branches should have the branch object, even if not loaded not loaded
        // should return updated branch details. the only that can change is the active branch commit

        for(int i=0; i < branches.size(); i++){
            BranchData branchDetails = branches.get(i);
            if(branchDetails.getName().equals(activeBranch.getName())){
                branchDetails.setHeadSha1(activeBranch.getHead().getCommitSHA1());
                branchDetails.setHeadMsg(activeBranch.getHead().getMsg());
            }
        }
    }

    Map<String ,List<String>> getWorkingCopy(){
        return activeBranch.getWorkingCopy();
    }

    void createNewBranch(String branchName, boolean checkout) throws UncommittedChangesError, InvalidBranchNameError{
        if(branches.stream().anyMatch(branch-> branch.getName().equals(branchName)) ||
            branchName.contains(" "))
            throw new InvalidBranchNameError("");

        Branch newBranch;
        if (activeBranch == null){
            newBranch = new Branch(branchName);
        }
        else{
            newBranch = new Branch(branchName, activeBranch.getHead(),
                    activeBranch.getRootFolder());
        }
        addNewBranch(newBranch);

        if (checkout){
            if (activeBranch != null && haveOpenChanges())
                throw new UncommittedChangesError("Cannot checkout on open changes");

            setActiveBranch(newBranch);
        }
    }

    void setActiveBranch(Branch branch){
        activeBranch = branch;
        saveRepositoryActiveBranch();
    }

    void checkoutBranch(String name, boolean force) throws UncommittedChangesError, InvalidBranchNameError {
        if (!validBranchName(name))
            throw new InvalidBranchNameError("InvalidBranchNameError");

        if(activeBranch.haveChanges() && !force)
            throw new UncommittedChangesError("UncommittedChangesError");

        Utils.clearCurrentWC();

        Branch checkedoutBranch = Branch.load(name, true);
        setActiveBranch(checkedoutBranch);
    }

    void deleteBranch(String branchName) throws InvalidBranchNameError{
        if (!validBranchName(branchName))
            throw new InvalidBranchNameError("InvalidBranchNameError");

        if(activeBranch.getName().equals(branchName)){
            throw new IllegalArgumentException();
        }

        deleteBranchFromHistory(branchName);
        Branch.deleteBranch(branchName);
    }

    private void deleteBranchFromHistory(String branchName){
        int i;
        for(i = 0; i< branches.size(); i++){
            if(branches.get(i).getName().equals(branchName))
                break;
        }

        branches.remove(i);
    }

    boolean haveOpenChanges(){ return activeBranch.haveChanges();}

    private void saveRepositoryDetails(){
        Utils.writeFile(Settings.repositoryDetailsFilePath, name, false);
    }

    private void saveRepositoryActiveBranch(){
        Utils.writeFile(Settings.activeBranchFilePath, activeBranch.getName(), false);
    }

    boolean validBranchName(String branchName) {
        return branches.stream().
                map(branch -> branch.getName()).
                anyMatch(name -> name.equals(branchName));
    }

    public void addNewBranch(Branch branch){
        branches.add(Branch.getBranchDisplayData(branch.getName()));
    }

    Map<String, Commit> getAllCommits(){
        Map<String, Commit> allCommitsData = new HashMap<>();

        for(BranchData branch: branches){
            String topCommitSha1 = branch.getHeadSha1();
            if (allCommitsData.get(topCommitSha1) == null)
                allCommitsData.put(topCommitSha1, new Commit(topCommitSha1));
        }

        return allCommitsData;
    }
}
