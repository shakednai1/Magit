package core;

import exceptions.NoChangesToCommitError;
import models.BranchData;
import models.CommitData;

import java.util.LinkedList;
import java.util.List;

public class Merge {

    String firstCommitSha1;
    String secondCommitSha1;
    List<FileChanges> conflicts = new LinkedList<>();
    FolderChanges folderChanges;
    BranchData mergingBranch;

    private String commitMsg = "";


    public Merge(String firstCommitSha1, String secondCommitSha1, BranchData branchToMerge){
        MainEngine engine = new MainEngine();
        this.mergingBranch = branchToMerge;
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;
        folderChanges = engine.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1);
        if(folderChanges.getHasConflicts()){
            setConflictFiles();
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
        folderChanges.unfoldFS();
        RepositoryManager repositoryManager = MainEngine.getRepositoryManager();
        Branch activeBranch = repositoryManager.getActiveRepository().getActiveBranch();

        Commit commit = activeBranch.mergeCommit(this);
        return new CommitData(commit);
    }


    public void setCommitMsg(String msg){
        commitMsg = msg;
    }


}
