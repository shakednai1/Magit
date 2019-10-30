package core;


import models.CommitData;

public class CommitDataComperator implements java.util.Comparator<CommitData> {
    public int compare(CommitData c1, CommitData c2) {
        return c1.getCommitTimeDate().compareTo(c2.getCommitTimeDate());
    }
}
