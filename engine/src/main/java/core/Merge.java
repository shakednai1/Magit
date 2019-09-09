package core;

import java.util.LinkedList;
import java.util.List;

public class Merge {

    String firstCommitSha1;
    String secondCommitSha1;
    List<FileChanges> conflicts = new LinkedList<>();


    public Merge(String firstCommitSha1, String secondCommitSha1){
        MainEngine engine = new MainEngine();
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;
        FolderChanges folderChanges = engine.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1);
        if(folderChanges.getHasConflicts()){
            getConflictFiles(folderChanges);
        }
    }

    public List<FileChanges> getConflicts(){
        return conflicts;
    }

    public void getConflictFiles(FolderChanges folder){
        for(FileChanges file : folder.getSubChangesFiles()){
            Common.FilesStatus status = file.getState();
            if(status == Common.FilesStatus.CONFLICTED){
                conflicts.add(file);
            }
        }
        for(FolderChanges subFolder: folder.getSubChangesFolders()){
            if(subFolder.getHasConflicts()){
                getConflictFiles(subFolder);
            }
        }
    }


}
