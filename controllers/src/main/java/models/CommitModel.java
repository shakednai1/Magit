package models;

import java.util.ArrayList;
import java.util.List;


public class CommitModel {

    private String sha1;
    private String message;
    private String committer;
    private String commitTime;
    private String previousCommitSha1;
    private boolean isInMasterChain;

    private List<BranchModel> pointingBranches = new ArrayList<>();



    public CommitModel(String sha1, String message, String committer,
                String commitTime, String previousCommitSha1,
                       boolean isInMasterChain){
        this.sha1 = sha1;
        this.message = message;
        this.committer = committer;
        this.commitTime = commitTime;
//        this.previousCommitSha1 = previousCommitSha1.equals("null")? null : previousCommitSha1;
        this.previousCommitSha1 = previousCommitSha1;
        this.isInMasterChain = isInMasterChain;
    }

    public String getSha1() {
        return sha1;
    }

    public String getMessage() {
        return message;
    }

    public String getCommitter() {
        return committer;
    }

    public String getCommitTime() { return commitTime; }

    public String getPreviousCommitSha1() {
        return previousCommitSha1;
    }

    public boolean getIsInMasterChain() {
        return isInMasterChain;
    }

    void addPointingBranch(BranchModel branch){
        pointingBranches.add(branch);
    }

}
