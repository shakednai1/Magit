package models;

import core.Commit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;


public class CommitData {

    private String sha1;
    private String message;
    private String committer;
    private String commitTime;
    private String previousCommitSha1 = null;
    private String secondPreviousCommitSha1 = null;
    // TODO property -  add label from commitNodeController
    private ObservableList<BranchData> pointingBranches = FXCollections.observableArrayList();
    private ObservableList<BranchData> containingBranches = FXCollections.observableArrayList();

    private boolean isInMasterChain = false;


    public CommitData(Commit commit){
        this.sha1 = commit.getSha1();
        this.message = commit.getMsg();
        this.committer = commit.getUserLastModified();
        this.commitTime = commit.getCommitTime();
        this.previousCommitSha1 = commit.getFirstPrecedingSha1();
        this.secondPreviousCommitSha1 = commit.getSecondPrecedingSha1();
    }

    public String getSha1() { return sha1; }

    public String getMessage() { return message; }

    public String getCommitter() { return committer; }

    public String getCommitTime() { return commitTime; }

    public String getPreviousCommitSha1() { return previousCommitSha1; }

    public String getSecondPreviousCommitSha1() { return secondPreviousCommitSha1; }

    public boolean getIsInMasterChain() { return isInMasterChain; }

    public void addPointingBranch(BranchData branch){
        if(pointingBranches.contains(branch)) return;
        pointingBranches.add(branch);
    }

    public void removePointingBranch(BranchData branch){ pointingBranches.remove(branch); }

    public void addContainingBranch(BranchData branch){
        if(containingBranches.contains(branch)) return;
        containingBranches.add(branch);
    }

    public void removeContainingBranch(BranchData branch){ containingBranches.remove(branch); }

    public ObservableList<BranchData> getContainingBranches(){ return containingBranches; }


    public void setInMasterChain(){ isInMasterChain = true; }

    public ObservableList<BranchData> getPointingBranches(){ return pointingBranches; }


    public List<String> getPointingBranchNames(){
        return pointingBranches.stream().map(BranchData::getName).sorted().collect(Collectors.toList());
    }

}
