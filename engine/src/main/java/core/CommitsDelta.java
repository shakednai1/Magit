package core;

import models.BranchData;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.util.HashMap;
import java.util.Map;

public class CommitsDelta {
    
    final private Commit baseCommit;
    final private Commit commitA;
    final private Commit commitB;
    
    private FolderChanges rootFolderChanges;

    public CommitsDelta(String sha1A, String sha1B){
        this.commitA = new Commit(sha1A);
        this.commitB = new Commit(sha1B);
        baseCommit = findAncestor();
    }
    
    private Commit findAncestor(){
        AncestorFinder ancestorFinder = new AncestorFinder( (sha1) -> (new Commit(sha1)));
        String ancestorSha1 = ancestorFinder.traceAncestor(commitA.getSha1(),commitA.getSha1());
        return new Commit(ancestorSha1);
    }

    public void calcFilesMergeState(){

        Folder baseFolder = Commit.getCommitRootFolder(this.baseCommit.getSha1());
        Folder aFolder = Commit.getCommitRootFolder(this.commitA.getSha1());
        Folder bFolder = Commit.getCommitRootFolder(this.commitB.getSha1());

        rootFolderChanges = new FolderChanges(baseFolder, aFolder, bFolder);
    }


    protected void commit(){}
    
    
    
    

}
