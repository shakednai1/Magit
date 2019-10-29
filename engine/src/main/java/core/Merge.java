package core;

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
    Branch activeBranch;
    String fromBranch;
    String toBranch;
    String fastForwardSha1 = null;
    Settings repoSettings;

    private String commitMsg = "";


    public Merge(String firstCommitSha1, String secondCommitSha1,
                 String fromBranch, String toBranch,
                 Settings repoSettings, Branch activeBranch){

        this.fromBranch = fromBranch;
        this.toBranch = toBranch;
        this.firstCommitSha1 = firstCommitSha1;
        this.secondCommitSha1 = secondCommitSha1;
        this.activeBranch = activeBranch;
        this.repoSettings = activeBranch.getRepoSettings();

        setFastForward();
        if (fastForwardSha1 == null) {
            folderChanges = CommitsDelta.getDiffBetweenCommits(firstCommitSha1, secondCommitSha1, repoSettings);
            if (folderChanges.getHasConflicts()) {
                setConflictFiles();
            }
        }
    }

    public String getToBranch(){ return toBranch; }
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
        mergeTime = Settings.commitDateFormat.format(new Date());
        activeBranch.mergeCommit(this);

        Commit com = new Commit(getCommitMsg(), folderChanges.getSha1(),
                folderChanges.userLastModified, mergeTime,
                getFirstCommitSha1(), getSecondCommitSha1(),
                repoSettings);
        com.zipCommit();

        activeBranch.setHead(com);
        return com;
    }

    public void setCommitMsg(String msg){
        commitMsg = msg;
    }

    void setFastForward() {

        Commit firstCommit = new Commit(firstCommitSha1, repoSettings);
        Commit secondCommit = new Commit(secondCommitSha1, repoSettings);

        Date firstCommitTime = null;
        Date secondCommitTime = null;
        try{
            firstCommitTime = Settings.commitDateFormat.parse(firstCommit.getCommitTime());
            secondCommitTime = Settings.commitDateFormat.parse(secondCommit.getCommitTime());
        }
        catch(ParseException e){}

        Map<String, Commit> commitParents;
        if(firstCommitTime.after(secondCommitTime) || firstCommitTime.equals(secondCommitTime)){
            commitParents = Commit.loadAll(firstCommit.getSha1(), repoSettings);
            if(commitParents.containsKey(secondCommitSha1)){
                fastForwardSha1 = firstCommitSha1;
            }
        }
        else if(firstCommitTime.before(secondCommitTime)){
            commitParents = Commit.loadAll(secondCommit.getSha1(), repoSettings);
            if(commitParents.containsKey(firstCommitSha1)){
                fastForwardSha1 = secondCommitSha1;
            }
        }
    }

    public String getFastForward(){
        return fastForwardSha1;
    }

    public boolean isFastForwardSha1(){
        return fastForwardSha1 !=null;
    }

}
