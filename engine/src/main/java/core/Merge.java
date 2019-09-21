package core;

import exceptions.NoActiveRepositoryError;
import exceptions.NoChangesToCommitError;
import models.BranchData;
import models.CommitData;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Merge {

    String firstCommitSha1;
    String secondCommitSha1;
    String mergeTime;
    List<FileChanges> conflicts = new LinkedList<>();
    FolderChanges folderChanges;
    String mergingBranchName;
    String fastForward = null;

    private String commitMsg = "";


    public Merge(String firstCommitSha1, String secondCommitSha1, String branchToMerge){
        MainEngine engine = new MainEngine();
        this.mergingBranchName = branchToMerge;
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;

        setFastForward();
        if (fastForward == null) {
            folderChanges = engine.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1);
            if (folderChanges.getHasConflicts()) {
                setConflictFiles();
            }
        }
    }

    public String getMergingBranchName(){ return mergingBranchName; }
    public String getCommitMsg(){ return commitMsg;}
    public String getFirstCommitSha1(){ return firstCommitSha1;}
    public String getSecondCommitSha1(){ return secondCommitSha1;}

    public List<FileChanges> getConflicts(){
        return conflicts;
    }

    public void setConflictFiles(){
        conflicts = new LinkedList<>();
        setConflictFilesRec(this.folderChanges);

    }

    private void setConflictFilesRec(FolderChanges folder){
        for(FileChanges file : folder.getSubChangesFiles().values()){
            Common.FilesStatus status = file.getState();
            if(status == Common.FilesStatus.CONFLICTED){
                conflicts.add(file);
            }
        }
        for(FolderChanges subFolder: folder.getSubChangesFolders().values()){
            if(subFolder.getHasConflicts()){
                setConflictFilesRec(subFolder);
            }
        }
    }

    public Commit commit(){
        RepositoryManager repositoryManager = MainEngine.getRepositoryManager();
        Branch activeBranch = repositoryManager.getActiveRepository().getActiveBranch();

        mergeTime = Settings.commitDateFormat.format(new Date());
        activeBranch.mergeCommit(this);

        Commit com = new Commit(getCommitMsg(), folderChanges.getSha1(),
                folderChanges.userLastModified, mergeTime,
                getFirstCommitSha1(), getSecondCommitSha1());
        com.zipCommit();

        activeBranch.setHead(com);
        return com;
    }

    public void setCommitMsg(String msg){
        commitMsg = msg;
    }

    void setFastForward() {

        Commit firstCommit = new Commit(firstCommitSha1);
        Commit secondCommit = new Commit(secondCommitSha1);

        Date firstCommitTime = null;
        Date secondCommitTime = null;
        try{
            firstCommitTime = Settings.commitDateFormat.parse(firstCommit.getCommitTime());
            secondCommitTime = Settings.commitDateFormat.parse(secondCommit.getCommitTime());
        }
        catch(ParseException e){}

        Map<String, Commit> commitParents;
        if(firstCommitTime.after(secondCommitTime) || firstCommitTime.equals(secondCommitTime)){
            commitParents = Commit.loadAll(firstCommit.getSha1());
            if(commitParents.containsKey(secondCommitSha1)){
                fastForward = firstCommitSha1;
            }
        }
        else if(firstCommitTime.before(secondCommitTime)){
            commitParents = Commit.loadAll(secondCommit.getSha1());
            if(commitParents.containsKey(firstCommitSha1)){
                fastForward = secondCommitSha1;
            }
        }
    }

    public String getFastForward(){
        return fastForward;
    }

    public boolean isFastForward(){
        return fastForward !=null;
    }

}
