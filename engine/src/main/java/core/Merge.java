package core;

import exceptions.NoActiveRepositoryError;
import exceptions.NoChangesToCommitError;
import models.BranchData;
import models.CommitData;

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
    BranchData mergingBranch;
    boolean fastForward;

    private String commitMsg = "";


    public Merge(String firstCommitSha1, String secondCommitSha1, BranchData branchToMerge){
        MainEngine engine = new MainEngine();
        this.mergingBranch = branchToMerge;
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;
        if (!DoNothing()) {
            setFastForward();
            if (fastForward) {
                try {
                    engine.getActiveBranch().setHead(new Commit(secondCommitSha1));
                    engine.getActiveRepo().updateActiveBranchDataInHistory();
                } catch (NoActiveRepositoryError e) {
                }
            } else {
                folderChanges = engine.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1);
                if (folderChanges.getHasConflicts()) {
                    setConflictFiles();
                }
            }
        }
    }

    public BranchData getMergingBranch(){ return mergingBranch; }
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

    public CommitData commit(){
        RepositoryManager repositoryManager = MainEngine.getRepositoryManager();
        Branch activeBranch = repositoryManager.getActiveRepository().getActiveBranch();

        mergeTime = Settings.commitDateFormat.format(new Date());


        activeBranch.mergeCommit(this);

        Commit com = new Commit(getCommitMsg(), folderChanges.getSha1(),
                folderChanges.userLastModified, mergeTime,
                getFirstCommitSha1(), getSecondCommitSha1());
        com.zipCommit();

        activeBranch.setHead(com);

        return new CommitData(com);
    }


    public void setCommitMsg(String msg){
        commitMsg = msg;
    }

    void setFastForward() {
        Map<String, Commit> commitParents = Commit.loadAll(secondCommitSha1);
        if (commitParents.keySet().contains(firstCommitSha1)) {
            fastForward = true;
        } else {
            fastForward = false;
        }
    }

    public boolean getFastForward(){
        return fastForward;
    }
    private boolean DoNothing() {
        Map<String, Commit> commitParents = Commit.loadAll(firstCommitSha1);
        if (commitParents.keySet().contains(secondCommitSha1)) {
            fastForward = true;
            return true;
        } else {
            fastForward = false;
            return false;
        }
    }

}
