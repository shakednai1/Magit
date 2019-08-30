package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class CommitData {

    // TODO - duplication of code. DISASTER

    private String sha1;
    private String message;
    private String committer;
    private String commitTime;
    private String previousCommitSha1;
    private Set<BranchData> pointingBranches = new HashSet<>();

    private boolean isInMasterChain = false;


    public CommitData(String sha1, String message, String committer,
                      String commitTime, String previousCommitSha1){
        this.sha1 = sha1;
        this.message = message;
        this.committer = committer;
        this.commitTime = commitTime;
        this.previousCommitSha1 = previousCommitSha1.equals("null")? null : previousCommitSha1;
    }

    public String getSha1() { return sha1; }

    public String getMessage() { return message; }

    public String getCommitter() { return committer; }

    public String getCommitTime() { return commitTime; }

    public String getPreviousCommitSha1() { return previousCommitSha1; }

    public boolean getIsInMasterChain() { return isInMasterChain; }


    public void addPointingBranch(BranchData branch){ pointingBranches.add(branch); }

    public void setInMasterChain(){ isInMasterChain = true; }

}
