package core;

import exceptions.InvalidBranchNameError;
import exceptions.NoActiveRepositoryError;
import exceptions.NoChangesToCommitError;
import exceptions.UncommittedChangesError;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import models.CommitData;
import models.BranchData;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Repository {

    private String name;
    private String fullPath;
    private Branch activeBranch = null;
    private ObservableList<BranchData> branches = FXCollections.observableArrayList();
    private ObservableMap<String, CommitData> commits = FXCollections.observableHashMap();

    private String remoteRepositoryPath = null;
    private String remoteRepositoryName = null;
    private List<RemoteBranch> remoteBranches = new LinkedList<>();

    Merge currentMerge;

    class BranchHeadCommitListener implements ChangeListener<String> {

        final private BranchData branchData;

        BranchHeadCommitListener(BranchData branch){ branchData = branch;}

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            CommitData oldCommit = commits.get(oldValue);
            if(oldCommit != null)
                oldCommit.removePointingBranch(branchData);
            commits.get(newValue).addPointingBranch(branchData);
        }
    }

    class CommitsListener implements MapChangeListener<String , CommitData> {

        @Override
        public void onChanged(Change<? extends String, ? extends CommitData> change) {
            if(change.wasAdded()){
                CommitData newCommit = change.getValueAdded();

                for (BranchData branch: branches){
                    if(branch.getHeadSha1().equals(newCommit.getSha1())){
                        newCommit.addPointingBranch(branch);
                    }
                }
            }
        }
    }

    class BranchesListener implements ListChangeListener<BranchData>{

        @Override
        public void onChanged(Change<? extends BranchData> c) {

            if (c.wasAdded()){
                BranchData branchData = c.getAddedSubList().get(0);
                if(commits.get(branchData.getHeadSha1()) != null)
                    commits.get(branchData.getHeadSha1()).addPointingBranch(branchData);
            }

            else if (c.wasRemoved()){
                BranchData branchData = c.getRemoved().get(0);

                if(commits.containsKey(branchData.getHeadSha1()))
                    commits.get(branchData.getHeadSha1()).removePointingBranch(branchData);
            }
        }
    }


    Repository(String fullPath, String name, boolean empty) {
        this.fullPath = fullPath;
        this.name = name;

        commits.addListener(new CommitsListener());
        branches.addListener(new BranchesListener());

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

        commits.addListener(new CommitsListener());
        branches.addListener(new BranchesListener());

        loadRemoteRepoDetails();
        loadRemoteBranches();

        loadBranchesData();
        loadAllCommitsData();

        this.activeBranch = activeBranch;
        saveRepositoryActiveBranch();
    }

    private void loadRemoteRepoDetails(){
        if(isRemote()){
            List<String> remoteData = FSUtils.getFileLines(Settings.repositoryRemoteDetailsFilePath);
            String[] remoteDetails = remoteData.get(0).split(Settings.delimiter);
            this.remoteRepositoryName = remoteDetails[1];
            this.remoteRepositoryPath = remoteDetails[0];
        }
    }

    public boolean isRemote(){
        File remoteFile = new File(Settings.repositoryRemoteDetailsFilePath);
        return remoteFile.exists();
    }

    private void loadRemoteBranches(){
        if(remoteRepositoryName != null){
            remoteBranches = new LinkedList<>();
            File remoteBranchesFolder = new File(Settings.remoteBranchesPath);
            for(File branch : remoteBranchesFolder.listFiles()){
                if(!branch.getName().equals("HEAD")){
                    String pointedCommit = FSUtils.getFileLines(branch.getPath()).get(0).split(Settings.delimiter)[0];
                    RemoteBranch remoteBranch = new RemoteBranch(branch.getName().split(".txt")[0], pointedCommit);
                    remoteBranches.add(remoteBranch);
                }
            }
        }
    }

    private String loadRepositoryName(){
        List<String> repoDetails = FSUtils.getFileLines(Settings.repositoryDetailsFilePath);
        return repoDetails.get(0);
    }

    String getFullPath(){ return fullPath; }
    String getName(){ return name; }
    Branch getActiveBranch(){ return activeBranch; }
    ObservableList<BranchData> getAllBranches(){ return branches; }
    Merge getCurrentMerge(){ return currentMerge; }


    static Repository load(String repositoryPath){
        // we know that the repo exists and valid

        Settings.setNewRepository(repositoryPath);

        List<String> contentByLines = FSUtils.getFileLines(Settings.activeBranchFilePath);
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
                BranchData branchData = Branch.getBranchDisplayData(name[0], this);
                branchData.getHeadSha1Property().addListener(new BranchHeadCommitListener(branchData));
                branches.add(branchData);
            }
        }

    }
    ObservableMap<String, CommitData> getAllCommitsData(){ return commits; }


    private void loadAllCommitsData(){
        commits.clear();
        for(BranchData branch: branches){
            __addBranchCommitsToAllCommits(branch);
            commits.get(branch.getHeadSha1()).addPointingBranch(branch);
        }
    }

    private void __addBranchCommitsToAllCommits(BranchData branch){
        boolean isMaster = branch.getName().equals("master");

        for(Map.Entry<String, Commit> c: Commit.loadAll(branch.getHeadSha1()).entrySet()){
            if(commits.get(c.getKey()) == null){
                Commit commit = c.getValue();
                CommitData commitData = new CommitData(commit);
                commits.put(c.getKey(), commitData);
            }

            if(isMaster)
                commits.get(c.getKey()).setInMasterChain();
        }

    }

    CommitData  commitActiveBranch(String msg) throws NoChangesToCommitError {
        // this function is for assert that branch details at `branches` object will stay updated

        Commit commit = activeBranch.commit(msg, null);
        CommitData commitData = __createCommitData(commit);
        commits.put(commitData.getSha1(), commitData);

        updateActiveBranchDataInHistory();

        return commitData;
    }

    private CommitData __createCommitData(Commit commit){
        CommitData commitData = new CommitData(commit);
        if (activeBranch.getName().equals("master"))
            commitData.setInMasterChain();
        return commitData;
    }

    private void updateActiveBranchDataInHistory(){

        for(int i=0; i < branches.size(); i++){
            BranchData branchDetails = branches.get(i);
            if(branchDetails.getName().equals(activeBranch.getName())){
                branchDetails.setHeadSha1(activeBranch.getHead().getSha1());
                branchDetails.setHeadMsg(activeBranch.getHead().getMsg());
            }
        }
    }

    public void resetActiveBranch(String commitSha1) throws NoActiveRepositoryError {
        String branchName = activeBranch.getName();
        String trackingAfter = activeBranch.getTrackingAfter();

        Branch branch = new Branch(branchName, commitSha1, trackingAfter, true);
        setActiveBranch(branch);

        activeBranch.getRootFolder().updateState();

        updateActiveBranchDataInHistory();

    }

    FilesDelta getWorkingCopy(){
        return activeBranch.getWorkingCopy();
    }

    BranchData createNewBranch(String branchName, boolean checkout) throws UncommittedChangesError, InvalidBranchNameError{
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
        BranchData branchData = addNewBranch(newBranch);

        if (checkout){
            if (activeBranch != null && haveOpenChanges())
                throw new UncommittedChangesError("Cannot checkout on open changes");

            setActiveBranch(newBranch);
        }

        return branchData;

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

        FSUtils.clearCurrentWC();

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
        FSUtils.writeFile(Settings.repositoryDetailsFilePath, name, false);
    }

    private void saveRepositoryActiveBranch(){
        FSUtils.writeFile(Settings.activeBranchFilePath, activeBranch.getName(), false);
    }

    boolean validBranchName(String branchName) {
        return branches.stream().
                map(BranchData::getName).
                anyMatch(name -> name.equals(branchName));
    }

    public BranchData addNewBranch(Branch branch){
        BranchData branchData = new BranchData(branch);
        branchData.getHeadSha1Property().addListener(new BranchHeadCommitListener(branchData));

        branches.add(branchData);

        return branchData;
    }

    public void setRemoteRepositoryPath(String RRpath){
        remoteRepositoryPath = RRpath;
    }
    public void setRemoteRepositoryName(String RRname){
        remoteRepositoryName = RRname;
    }
    public String getRemoteRepositoryName(){
        return remoteRepositoryName;
    }


    public void addRemoteBranch(RemoteBranch remoteBranch){
        remoteBranches.add(remoteBranch);
        FSUtils.createNewFile(Settings.remoteBranchesPath + remoteBranch.name + ".txt",
                remoteBranch.pointedCommitSha1);
    }

    public List<RemoteBranch> getAllRemoteBranches(){
        return remoteBranches;
    }

    public boolean hasRemoteRepo(){
        return remoteRepositoryName != null;
    }

    public void fetch(){
        File branchesDir = new File(remoteRepositoryPath + Settings.branchFolder);
        File remoteBranchesDir = new File(Settings.remoteBranchesPath);
        try {
            FileUtils.copyDirectory(branchesDir,remoteBranchesDir);
        }
        catch (IOException e){
        }

        loadRemoteBranches();

        File sourceObjectsDir = new File(remoteRepositoryPath + Settings.objectsFolder);
        for(File file : sourceObjectsDir.listFiles()){
            File destFile = new File(Settings.objectsFolderPath + "/" + file.getName());
            try {
                FileUtils.copyFile(file, destFile);
            } catch (IOException e) {
            }
        }
    }

    BranchData createNewBranchFromSha1(String branchName, String sha1, String trackingAfter){
        Branch newBranch = new Branch(branchName, sha1, trackingAfter, false);
        return addNewBranch(newBranch);

    }

    public boolean isValidBranchName(String name) throws InvalidBranchNameError {
        if(branches.stream().anyMatch(branch-> branch.getName().equals(name)) ||
                name.contains(" ")){
            throw new InvalidBranchNameError("");
        }
        return true;
    }

    public Merge getMerge(String branchName) throws ValueException{
        String commitSha1 = "";
        List<BranchData> branchesData = getAllBranches();
        BranchData mergeBranch = null;

        for(BranchData branchData: branchesData){
            if(branchData.getName().equals(branchName)){
                commitSha1 = branchData.getHeadSha1();
                mergeBranch = branchData;
                break;
            }
        }

        if (mergeBranch == null) throw new ValueException("Invalid branch name");

        currentMerge = new Merge(getActiveBranch().getHead().getSha1(), commitSha1, mergeBranch);
        return currentMerge;
    }

    public void makeMerge(Merge merge){
        merge.setConflictFiles();
        if(merge.getConflicts().size() != 0){ return; }

        CommitData commitData = merge.commit();
        commits.put(commitData.getSha1(), commitData);
        updateActiveBranchDataInHistory();

        currentMerge = null;
    }



    public Merge pull(){
        String trackingAfterBranchName = getActiveBranch().getTrackingAfter();
        String pointingCommitOfRemoteBranch = getPointingCommitOfRB(trackingAfterBranchName);
        updateRBDataFromRemote(trackingAfterBranchName);
        getObjectsFromRemote();
        BranchData activeBranchData = null;
        for(BranchData branchData : getAllBranches()){
            if(branchData.getName().equals(getActiveBranch().getName())){
                activeBranchData = branchData;
            }
        }
        return new Merge(getActiveBranch().getHead().getSha1(), pointingCommitOfRemoteBranch, activeBranchData);
    }

    public void push(){
        pushObjectsToRemote();
        updateRBDataFromLocal(getActiveBranch().getTrackingAfter());
    }

    private void updateRBDataFromLocal(String remoteBranchName){
        File destBranchFile= new File(Settings.remoteBranchesPath + remoteBranchName + ".txt");
        File sourceBranchFile = new File(Settings.branchFolderPath + remoteBranchName + ".txt");
        File destBranchFileRemote= new File(remoteRepositoryPath + Settings.branchFolder + remoteBranchName + ".txt");
        try {
            FileUtils.copyFile(sourceBranchFile, destBranchFile);
            FileUtils.copyFile(destBranchFile, destBranchFileRemote);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updatePointingBranchOfRB(remoteBranchName);
    }

    private void pushObjectsToRemote(){
        File sourceObjFolder = new File(Settings.objectsFolderPath);
        try {
            for (File file : sourceObjFolder.listFiles()) {
                File destFile = new File(remoteRepositoryPath + Settings.objectsFolder + "/" + file.getName());
                FileUtils.copyFile(file, destFile);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getObjectsFromRemote(){
        File sourceObjFolder = new File(remoteRepositoryPath + Settings.objectsFolder);
        try {
            for (File file : sourceObjFolder.listFiles()) {
                File destFile = new File(Settings.objectsFolderPath + "/" + file.getName());
                FileUtils.copyFile(file, destFile);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateRBDataFromRemote(String remoteBranchName){
        File sourceBranchFile = new File(remoteRepositoryPath + Settings.branchFolder + remoteBranchName + ".txt");
        File destBranchFile = new File(Settings.remoteBranchFolder + remoteBranchName + ".txt");
        try {
            FileUtils.copyFile(sourceBranchFile, destBranchFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        updatePointingBranchOfRB(remoteBranchName);

    }

    private void updatePointingBranchOfRB(String remoteBranchName){
        String pointingCommitOfRemoteBranch = getPointingCommitOfRB(remoteBranchName);
        for(RemoteBranch remoteBranch : remoteBranches){
            if(remoteBranch.getName().equals(remoteBranchName)){
                remoteBranch.pointedCommitSha1 = pointingCommitOfRemoteBranch;
            }
        }
    }

    private String getPointingCommitOfRB(String remoteBranchName){
        String branchDataFromFile = FSUtils.getFileLines(remoteRepositoryPath + Settings.remoteBranchFolder + remoteBranchName + ".txt").get(0);
        String pointingCommitOfRemoteBranch = branchDataFromFile.split(",")[0];
        return pointingCommitOfRemoteBranch;
    }

}
