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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Repository {

    private Settings settings;

    private String name;
    private String fullPath;
    private Branch activeBranch = null;
    private ObservableList<BranchData> branches = FXCollections.observableArrayList();
    private ObservableMap<String, CommitData> commits = FXCollections.observableHashMap();

    private RemoteRepository remoteRepository;

    Merge currentMerge;

    class BranchHeadCommitListener implements ChangeListener<String> {

        final private BranchData branchData;

        BranchHeadCommitListener(BranchData branch){ branchData = branch;}

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            CommitData oldCommit = commits.get(oldValue);
            if(oldCommit != null)
                oldCommit.removePointingBranch(branchData);
            if(commits.get(newValue) != null){
                commits.get(newValue).addPointingBranch(branchData);
            }
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

            c.next();
            if (c.wasAdded()){
                BranchData branchData = c.getAddedSubList().get(0);
                if(commits.get(branchData.getHeadSha1()) != null){
                    commits.get(branchData.getHeadSha1()).addPointingBranch(branchData);
                    addBranchToAllRelatedCommits(branchData);
                }
            }

            else if (c.wasRemoved()){
                BranchData branchData = c.getRemoved().get(0);

                if(commits.containsKey(branchData.getHeadSha1())){
                    commits.get(branchData.getHeadSha1()).removePointingBranch(branchData);
                    removeBranchFromAllRelatedCommits(branchData);
                }
            }
        }

        private void addBranchToAllRelatedCommits(BranchData branchData){
            String commitSha1 = branchData.getHeadSha1();

            List<String> commitsToExplore = new LinkedList<>();
            commitsToExplore.add(commitSha1);

            while (!commitsToExplore.isEmpty()){
                CommitData commitData = commits.get(commitsToExplore.get(0));
                commitData.addContainingBranch(branchData);
                if (commitData.getPreviousCommitSha1() != null && !commitData.getPreviousCommitSha1().equals(""))
                    commitsToExplore.add(commitData.getPreviousCommitSha1());

                if (commitData.getSecondPreviousCommitSha1() != null && !commitData.getSecondPreviousCommitSha1().equals(""))
                    commitsToExplore.add(commitData.getSecondPreviousCommitSha1());

                commitsToExplore.remove(commitsToExplore.get(0));
            }
        }

        private void removeBranchFromAllRelatedCommits(BranchData branchData){
            String commitSha1 = branchData.getHeadSha1();

            List<String> commitsToExplore = new LinkedList<>();
            commitsToExplore.add(commitSha1);

            while (!commitsToExplore.isEmpty()){
                CommitData commitData = commits.get(commitsToExplore.get(0));
                commitData.removeContainingBranch(branchData);

                if (commitData.getPreviousCommitSha1() != null && !commitData.getPreviousCommitSha1().equals(""))
                    commitsToExplore.add(commitData.getPreviousCommitSha1());

                if (commitData.getSecondPreviousCommitSha1() != null && !commitData.getSecondPreviousCommitSha1().equals(""))
                    commitsToExplore.add(commitData.getSecondPreviousCommitSha1());

                commitsToExplore.remove(commitsToExplore.get(0));
            }
        }

    }


    Repository(String fullPath, String name, boolean empty, Settings settings) {
        this.fullPath = fullPath;
        this.name = name;
        this.settings = settings;

        commits.addListener(new CommitsListener());
        branches.addListener(new BranchesListener());

        saveRepositoryDetails();

        if (!empty) {
            try {
                createNewBranch("master", true);
            } catch (UncommittedChangesError | InvalidBranchNameError e) { /* cant be ?*/ }
        }
    }

    private Repository(String fullPath, Branch activeBranch, Settings settings){
        this.settings = settings;
        this.fullPath = fullPath;
        this.name = loadRepositoryName();
        this.activeBranch = activeBranch;

        commits.addListener(new CommitsListener());
        branches.addListener(new BranchesListener());

        loadRemoteRepoDetails();

        loadBranchesData();
        if(!isEmptyRepo()) {
            loadAllCommitsData();
        }

        saveRepositoryActiveBranch();
    }

    Settings getSettings(){ return settings; }

    private void loadRemoteRepoDetails(){
        if(isRemote()){
            remoteRepository = new RemoteRepository(new File(settings.repositoryRemoteDetailsFilePath), settings);
        }
    }

    public boolean isRemote(){
        File remoteFile = new File(settings.repositoryRemoteDetailsFilePath);
        return remoteFile.exists();
    }

    private String loadRepositoryName(){
        List<String> repoDetails = FSUtils.getFileLines(settings.repositoryDetailsFilePath);
        return repoDetails.get(0);
    }

    String getFullPath(){ return fullPath; }
    public String getName(){ return name; }
    public Branch getActiveBranch(){ return activeBranch; }
    public ObservableList<BranchData> getAllBranches(){ return branches; }
    public BranchData getBranchByName(String name){
        return branches.stream().filter(b ->b.getName().equals(name)).collect(Collectors.toList()).get(0);
    }

    Merge getCurrentMerge(){ return currentMerge; }


    static Repository load(Settings settings){
        // we know that the repo exists and valid

        List<String> contentByLines = FSUtils.getFileLines(settings.activeBranchFilePath);
        String activeBranchName = contentByLines.get(0);

        Branch activeBranch = Branch.load(settings, activeBranchName, false);
        return new Repository(settings.repositoryFullPath, activeBranch, settings);
    }

    private void loadBranchesData(){
        branches.clear();

        File directory = new File(settings.branchFolderPath);
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

    private boolean isEmptyRepo(){
        return activeBranch.getHead() == null;
    }

    private void loadAllCommitsData(){
        commits.clear();
        for(BranchData branch: branches){
            __addBranchCommitsToAllCommits(branch);
            commits.get(branch.getHeadSha1()).addPointingBranch(branch);
        }

        __setCommitDataInMasterMainChain();

    }

    private void __addBranchCommitsToAllCommits(BranchData branch){
        for(Map.Entry<String, Commit> c: Commit.loadAll(branch.getHeadSha1(), settings).entrySet()){
            if(commits.get(c.getKey()) == null){
                Commit commit = c.getValue();
                CommitData commitData = new CommitData(commit);
                commits.put(c.getKey(), commitData);
            }
            commits.get(c.getKey()).addContainingBranch(branch);
        }
    }

    public Map<String, CommitData> getBranchCommits(BranchData branch) {
        Map<String, CommitData> res = new HashMap<>();
        for (Map.Entry<String, Commit> c : Commit.loadAll(branch.getHeadSha1(), settings).entrySet()) {
            res.put(c.getKey(), new CommitData(c.getValue()));
        }
        return res;
    }

    private void __setCommitDataInMasterMainChain(){
        BranchData masterBranch = getBranchDataByName("master");

        CommitData commitData = commits.get(masterBranch.getHeadSha1());

        while(commitData != null){
            commitData.setInMasterChain();
            commitData = commits.get(commitData.getPreviousCommitSha1());
        }
    }

    CommitData  commitActiveBranch(String msg) throws NoChangesToCommitError {
        // this function is for assert that branch details at `branches` object will stay updated

        Commit commit = activeBranch.commit(msg, null);
        CommitData commitData = __createCommitData(commit);
        updateActiveBranchDataInHistory();
        commits.put(commitData.getSha1(), commitData);


        return commitData;
    }

    private CommitData __createCommitData(Commit commit){
        CommitData commitData = new CommitData(commit);
        if (activeBranch.getName().equals("master"))
            commitData.setInMasterChain();
        return commitData;
    }

    public void updateActiveBranchDataInHistory(){

        for(int i=0; i < branches.size(); i++){
            BranchData branchDetails = branches.get(i);
            if(branchDetails.getName().equals(activeBranch.getName())){
                branchDetails.setHeadSha1(activeBranch.getHead().getSha1());
                branchDetails.setHeadMsg(activeBranch.getHead().getMsg());
                break;
            }
        }
    }

    public void resetActiveBranch(String commitSha1) throws NoActiveRepositoryError {
        FSUtils.clearWC(settings.repositoryFullPath);

        String branchName = activeBranch.getName();
        String trackingAfter = activeBranch.getTrackingAfter();

        Branch branch = new Branch(branchName, commitSha1, trackingAfter, true, settings);
        setActiveBranch(branch);

        activeBranch.getRootFolder().updateState();

        updateActiveBranchDataInHistory();

    }

    FilesDelta getWorkingCopy(){
        return activeBranch.getWorkingCopy();
    }

    public BranchData createNewBranch(String branchName, boolean checkout) throws UncommittedChangesError, InvalidBranchNameError{
        if (branchName.contains(" "))
            throw new InvalidBranchNameError("Branch name cannot contain spaces");

        if(branches.stream().anyMatch(branch-> branch.getName().equals(branchName))) {
            throw new InvalidBranchNameError(String.format("Branch with name \"%s\" already exist", branchName));
        }

        Branch newBranch;
        if (activeBranch == null){
            newBranch = new Branch(branchName, settings);
        }
        else{
            newBranch = new Branch(branchName, activeBranch.getHead(),
                    activeBranch.getRootFolder(), settings);
        }
        BranchData branchData = addNewBranchIfNotExist(newBranch);

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

    public void checkoutBranch(String name, boolean force) throws UncommittedChangesError, InvalidBranchNameError {
        if (!validBranchName(name))
            throw new InvalidBranchNameError("InvalidBranchNameError");

        if(activeBranch.haveChanges() && !force)
            throw new UncommittedChangesError("UncommittedChangesError");

        FSUtils.clearWC(settings.repositoryFullPath);

        Branch checkedoutBranch = Branch.load(settings, name, true);
        BranchData branchData = addNewBranchIfNotExist(checkedoutBranch);

        __addBranchCommitsToAllCommits(branchData);
        setActiveBranch(checkedoutBranch);
    }

    void deleteBranch(String branchName) throws InvalidBranchNameError{
        if (!validBranchName(branchName))
            throw new InvalidBranchNameError("InvalidBranchNameError");

        if(activeBranch.getName().equals(branchName)){
            throw new IllegalArgumentException();
        }

        deleteBranchFromHistory(branchName);
        _deleteBranch(branchName);
    }

    private void deleteBranchFromHistory(String branchName){
        int i;
        for(i = 0; i< branches.size(); i++){
            if(branches.get(i).getName().equals(branchName))
                break;
        }

        branches.remove(i);
    }

    private boolean _deleteBranch(String branchName){
        return FSUtils.deleteFile(settings.getBranchFilePath(branchName));
    }

    boolean haveOpenChanges(){ return activeBranch.haveChanges();}

    private void saveRepositoryDetails(){
        FSUtils.writeFile(settings.repositoryDetailsFilePath, name, false);
    }

    private void saveRepositoryActiveBranch(){
        FSUtils.writeFile(settings.activeBranchFilePath, activeBranch.getName(), false);
    }

    boolean validBranchName(String branchName) {
        return branches.stream().
                map(BranchData::getName).
                anyMatch(name -> name.equals(branchName));
    }

    private BranchData getBranchDataByName(String branchName) {
        return branches.stream()
                .filter((b)-> (b.getName().equals(branchName)))
                .collect(Collectors.toList()).get(0);
    }

    public BranchData addNewBranchIfNotExist(Branch branch){
        if(validBranchName(branch.getName()))
            return getBranchDataByName(branch.getName());

        BranchData branchData = new BranchData(branch);
        branchData.getHeadSha1Property().addListener(new BranchHeadCommitListener(branchData));

        branches.add(branchData);

        return branchData;
    }

    void setRemoteRepository(String RRpath, String name){
        remoteRepository = new RemoteRepository(name, new File(RRpath), settings);
    }

    public RemoteRepository getRemoteRepository() {
        return remoteRepository;
    }

    String getRemoteRepositoryName(){
        return remoteRepository.name;
    }
    String getRemoteRepositoryPath(){
        return remoteRepository.path.getAbsolutePath();
    }

    public List<RemoteBranch> getAllRemoteBranches(){
        return remoteRepository.getRemoteBranches();
    }

    public boolean hasRemoteRepo(){
        return remoteRepository != null;
    }

    public void fetch(){
        File branchesDir = new File(getRemoteRepositoryPath() + Settings.branchFolder);
        File remoteBranchesDir = new File(settings.remoteBranchesPath);
        try {
            FileUtils.copyDirectory(branchesDir,remoteBranchesDir);
        }
        catch (IOException e){
        }

        remoteRepository.loadRemoteBranches();

        File sourceObjectsDir = new File(getRemoteRepositoryPath() + Settings.objectsFolder);
        for(File file : sourceObjectsDir.listFiles()){
            File destFile = new File(settings.objectsFolderPath + "/" + file.getName());
            try {
                FileUtils.copyFile(file, destFile);
            } catch (IOException e) {
            }
        }
    }

    BranchData createNewBranchFromSha1(String branchName, String sha1, String trackingAfter){
        Branch newBranch = new Branch(branchName, sha1, trackingAfter, false, settings);
        return addNewBranchIfNotExist(newBranch);

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

        currentMerge = new Merge(getActiveBranch().getHead().getSha1(), commitSha1, activeBranch, mergeBranch.getName());
        return currentMerge;
    }

    public void makeFFMerge(Merge merge){

        if(merge.getFastForward() != null){
            Commit commit = new Commit(merge.getFastForward(), settings);

            if (!activeBranch.getHead().getSha1().equals(merge.getFastForward())){
                activeBranch.setHead(commit);
                activeBranch.setRootFolder(Commit.getCommitRootFolder(merge.getFastForward(), settings), true);

                updateActiveBranchDataInHistory();
            }

        }
        currentMerge = null;
    }

    public void makeMerge(Merge merge){

        merge.setConflictFiles();
        if(merge.getConflicts().size() != 0){ return; }

        Commit commit = merge.commit();
        CommitData commitData = __createCommitData(commit);
        updateActiveBranchDataInHistory();
        commits.put(commitData.getSha1(), commitData);

        currentMerge = null;
    }

    public Merge pull(){
        if(getActiveBranch().isTracking()){
            String trackingAfterBranchName = getActiveBranch().getTrackingAfter();
            String pointingCommitOfRemoteBranch = remoteRepository.getPointingCommitOfRB(trackingAfterBranchName);
            updateRBDataFromRemote(trackingAfterBranchName);
            getObjectsFromRemote();

            // load all new commits to repo
            for(Commit commit: Commit.loadAll(pointingCommitOfRemoteBranch, settings).values()){
                if (commits.get(commit.getSha1()) == null){
                    commits.put(commit.getSha1(), __createCommitData(commit));
                }
            }

            currentMerge = new Merge(getActiveBranch().getHead().getSha1(), pointingCommitOfRemoteBranch, activeBranch, trackingAfterBranchName);
            return currentMerge;
        }
        return null;
    }

    public void push(){
        if(!getActiveBranch().isTracking()){
            String branchFilePath = settings.branchFolderPath + getActiveBranch().getName() + ".txt";
            FSUtils.writeFile(branchFilePath, getActiveBranch().getHead().getSha1() + Settings.delimiter + getActiveBranch().getName(), false);
            RemoteBranch remoteBranch = new RemoteBranch(getActiveBranch().getName(), getActiveBranch().getHead().getSha1());
            getActiveBranch().trackingAfter = remoteBranch.name;
            remoteRepository.addRemoteBranch(remoteBranch);
        }
        String RBbranchName = getActiveBranch().isTracking() ? getActiveBranch().getTrackingAfter() : getActiveBranch().getName();
        pushObjectsToRemote();
        updateRBDataFromLocal(RBbranchName);
        rewriteRemoteFSIfNeeded();
    }

    private void rewriteRemoteFSIfNeeded(){
        if(activeBranch.getName().equals(getRemoteRepoBranchHeadName())){
            FSUtils.clearWC(getRemoteRepositoryPath());
            FSUtils.copyWC(settings.repositoryFullPath, getRemoteRepositoryPath());
        }
    }

    private String getRemoteRepoBranchHeadName(){
        File remoteHeadFile = new File(getRemoteRepositoryPath(), Settings.activeBranchFile);
        return FSUtils.getFileLines(remoteHeadFile.getAbsolutePath()).get(0);
    }

    private void updateRBDataFromLocal(String remoteBranchName){
        String rbPath = settings.remoteBranchesPath + remoteBranchName + ".txt";
        File branchFile= new File(rbPath);
        File destBranchFileRemote= new File(getRemoteRepositoryPath() + Settings.branchFolder + remoteBranchName + ".txt");
        try {
            FSUtils.writeFile(rbPath, getActiveBranch().getHead().getSha1() + ",null", false);
            FileUtils.copyFile(branchFile, destBranchFileRemote);
        } catch (IOException e) {
            e.printStackTrace();
        }
        remoteRepository.updatePointingBranchOfRB(remoteBranchName);
    }

    private void pushObjectsToRemote(){
        File sourceObjFolder = new File(settings.objectsFolderPath);
        try {
            for (File file : sourceObjFolder.listFiles()) {
                File destFile = new File(getRemoteRepositoryPath() + Settings.objectsFolder + "/" + file.getName());
                FileUtils.copyFile(file, destFile);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getObjectsFromRemote(){
        File sourceObjFolder = new File(getRemoteRepositoryPath() + Settings.objectsFolder);
        try {
            for (File file : sourceObjFolder.listFiles()) {
                File destFile = new File(settings.objectsFolderPath + "/" + file.getName());
                FileUtils.copyFile(file, destFile);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateRBDataFromRemote(String remoteBranchName){
        File sourceBranchFile = new File(getRemoteRepositoryPath() + Settings.branchFolder + remoteBranchName + ".txt");
        File destBranchFile = new File(Settings.remoteBranchFolder + remoteBranchName + ".txt");
        try {
            FileUtils.copyFile(sourceBranchFile, destBranchFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        remoteRepository.updatePointingBranchOfRB(remoteBranchName);

    }


}
