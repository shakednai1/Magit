package core;

import models.BranchData;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.util.HashMap;
import java.util.Map;

public class CommitsDelta {
    
    final private Commit baseCommit;
    final private Commit commitA;
    final private Commit commitB;

    private Settings repoSettings;
    private FolderChanges rootFolderChanges;

    public CommitsDelta(String sha1A, String sha1B, Settings repoSettings){
        this.repoSettings = repoSettings;

        this.commitA = new Commit(sha1A, repoSettings);
        this.commitB = new Commit(sha1B, repoSettings);
        baseCommit = findAncestor();
    }

    static public FolderChanges getDiffBetweenCommits(String commitSha1, String prevCommit, Settings repoSettings){
        CommitsDelta commitsDelta= new CommitsDelta(commitSha1, prevCommit, repoSettings);
        commitsDelta.calcFilesMergeState();
        return commitsDelta.getRootFolderChanges();
    }
    
    private Commit findAncestor(){
        AncestorFinder ancestorFinder = new AncestorFinder( (sha1) -> (new Commit(sha1, repoSettings)));
        String ancestorSha1 = ancestorFinder.traceAncestor(commitA.getSha1(),commitB.getSha1());
        return new Commit(ancestorSha1, repoSettings);
    }

    public void calcFilesMergeState(){

        Folder baseFolder = Commit.getCommitRootFolder(this.baseCommit.getSha1(), repoSettings);
        Folder aFolder = Commit.getCommitRootFolder(this.commitA.getSha1(), repoSettings);
        Folder bFolder = Commit.getCommitRootFolder(this.commitB.getSha1(), repoSettings);

        rootFolderChanges = new FolderChanges(baseFolder, aFolder, bFolder);
    }

    public FolderChanges getRootFolderChanges(){
        return rootFolderChanges;
    }

    protected void commit(){}

}
