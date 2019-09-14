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

    public List<FileChanges> getConflicts(){
        return conflicts;
    }

    public void setConflictFiles(){
        conflicts = new LinkedList<>();
        setConflictFilesRec(this.folderChanges);

    }

    private void setConflictFilesRec(FolderChanges folder){
        for(FileChanges file : folder.getSubChangesFiles()){
            Common.FilesStatus status = file.getState();
            if(status == Common.FilesStatus.CONFLICTED){
                conflicts.add(file);
            }
        }
        for(FolderChanges subFolder: folder.getSubChangesFolders()){
            if(subFolder.getHasConflicts()){
                setConflictFilesRec(subFolder);
            }
        }
    }

    public CommitData commit(){
        String msg = String.format("Merge %s into %s", MainEngine.getBranchMergeName(), MainEngine.getCurrentBranchName());

        folderChanges.unfoldFS();
        RepositoryManager repositoryManager = MainEngine.getRepositoryManager();
        Branch activeBranch = repositoryManager.getActiveRepository().getActiveBranch();

        Commit commit;
        try{
            commit = activeBranch.commit(commitMsg, secondCommitSha1);
        }
        catch (NoChangesToCommitError e){
            return null;
        }
        commit.zipCommit();

        return new CommitData(commit);

    }


    public void setCommitMsg(String msg){
        commitMsg = msg;
    }


}
