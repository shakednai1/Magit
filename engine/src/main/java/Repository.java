import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import models.CommitData;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import models.BranchData;

import java.util.*;

class Repository {

    private String name;
    private String fullPath;
    private Branch activeBranch = null;
    private List<BranchData> branches = new LinkedList<>();

    private String remoteRepositoryPath = null;
    private String remoteRepositoryName = null;
    private List<RemoteBranch> remoteBranches = new LinkedList<>();


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
                map(BranchData::getName).
                anyMatch(name -> name.equals(branchName));
    }

    public void addNewBranch(Branch branch){
        branches.add(Branch.getBranchDisplayData(branch.getName()));
    }

    Map<String, CommitData> getAllCommitsData(){
        Map<String, CommitData> allCommitsData = new HashMap<>();

        for(BranchData branch: branches){
            __addBranchCommitsToAllCommits(allCommitsData, branch);
        }

        return allCommitsData;
    }

    private void __addBranchCommitsToAllCommits(Map<String, CommitData> allCommitsData, BranchData branch){

        class CommitConvertor{
            CommitData convertCommitToData(Commit commit){
                return new CommitData(commit.getCommitSHA1(), commit.getMsg(),
                        commit.getUserLastModified(), commit.getCommitTime(), commit.getPreviousCommitSHA1());
            }
        }

        CommitConvertor commitConvertor = new CommitConvertor();
        boolean isMaster = branch.getName().equals("master");

        for(Map.Entry<String, Commit> c: Commit.loadAll(branch.getHeadSha1()).entrySet()){
            if(allCommitsData.get(c.getKey()) == null){
                Commit commit = c.getValue();
                CommitData commitData = commitConvertor.convertCommitToData(commit);
                allCommitsData.put(c.getKey(), commitData);
            }

            CommitData val = allCommitsData.get(c.getKey());

            val.addPointingBranch(branch);
            if(isMaster)
                val.setInMasterChain();
        }
    }


    public void setRemoteRepositoryPath(String RRpath){
        remoteRepositoryPath = RRpath;
    }
    public void setRemoteRepositoryName(String RRname){
        remoteRepositoryName = RRname;
    }

    public void addRemoteBranch(RemoteBranch remoteBranch){
        remoteBranches.add(remoteBranch);
    }

    public List<RemoteBranch> getAllRemoteBranches(){
        return remoteBranches;
    }

    public boolean hasRemoteRepo(){
        return remoteRepositoryName != null;
    }

    public void fetch(){
        remoteBranches = new LinkedList<>();
        File sourceBranchesDir = new File(remoteRepositoryPath + "/.magit/branches");
        for(File branch: sourceBranchesDir.listFiles()){
            if(!branch.getName().equals("HEAD")){
                remoteBranches.add(new RemoteBranch(remoteRepositoryName + "/" + branch.getName(),
                        Utils.getFileLines(branch.getPath()).get(0)));
            }
        }
        File sourceObjectsDir = new File(remoteRepositoryPath + "/.magit/objects");
        for(File file : sourceObjectsDir.listFiles()){
            File destFile = new File(Settings.objectsFolderPath + "/" + file.getName());
            try {
                FileUtils.copyFile(file, destFile);
            } catch (IOException e) {
            }
        }
    }
}