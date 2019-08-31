package models;

import core.Commit;
import javafx.beans.property.SetProperty;
import org.apache.commons.collections4.multiset.HashMultiSet;

import java.util.HashSet;
import java.util.Set;


public class CommitData {

    // TODO - duplication of code. DISASTER

    private String sha1;
    private String message;
    private String committer;
    private String commitTime;
    private String previousCommitSha1;
    // TODO property -  add label from commitNodeController
    private Set<BranchData> pointingBranches = new HashSet<>();

    private boolean isInMasterChain = false;


    public CommitData(Commit commit){
        this.sha1 = commit.getCommitSHA1();
        this.message = commit.getMsg();
        this.committer = commit.getUserLastModified();
        this.commitTime = commit.getCommitTime();
        this.previousCommitSha1 = commit.getPreviousCommitSHA1().equals("null") ? null: commit.getPreviousCommitSHA1();
    }

    public String getSha1() { return sha1; }

    public String getMessage() { return message; }

    public String getCommitter() { return committer; }

    public String getCommitTime() { return commitTime; }

    public String getPreviousCommitSha1() { return previousCommitSha1; }

    public boolean getIsInMasterChain() { return isInMasterChain; }

    public void addPointingBranch(BranchData branch){ pointingBranches.add(branch); }

    public void removePointingBranch(BranchData branch){ pointingBranches.remove(branch); }

    public void setInMasterChain(){ isInMasterChain = true; }

}
