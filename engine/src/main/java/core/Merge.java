package core;

import models.CommitData;

import java.util.LinkedList;
import java.util.List;

public class Merge {

    String firstCommitSha1;
    String secondCommitSha1;
    List<FileChanges> conflicts = new LinkedList<>();
    FolderChanges folderChanges;


    public Merge(String firstCommitSha1, String secondCommitSha1){
        MainEngine engine = new MainEngine();
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;
        folderChanges = engine.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1);
        if(folderChanges.getHasConflicts()){
            setConflictFiles();
        }
    }

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
        // open FS for folderChanges
        // commit active branch
        // set commit second
        // zip commit

        return null;
    }


}
