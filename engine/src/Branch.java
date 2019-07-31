import java.util.LinkedList;
import java.util.List;

class Branch {

    private Commit startCommit; //TODO add load commit - do not change it after define it
    private Commit head;
    private String name;
    private CommitManager commitManager;

    Branch(String name, Commit head){

        commitManager = new CommitManager();
        if(head == null){
            head = commitManager.commit("", true);
        }
        commitManager.setCurrentCommit(head);

        this.name = name;
        this.head = head;
        startCommit = head;

        writeBranchInfoFile();
    }

    Commit getHead(){ return head; }

    String getName(){
        return name;
    }

    CommitManager getCommitManager(){ return commitManager; }

    boolean haveChanges(){ return commitManager.haveChanges(); }

    boolean commit(String msg, boolean force){
        Commit newCommit = commitManager.commit(msg, force);

        if (newCommit == null)
            return false;

        setHead(newCommit);
        return true;
    }

    void setHead(Commit newHead) {
        head = newHead;
        writeBranchInfoFile();
    }

    void writeBranchInfoFile(){
        String branchFileContent =  head.getSHA1()+ Settings.delimiter + startCommit.getSHA1();
        Utils.writeFile(getBranchFilePath(), branchFileContent, false);
    }

    private String getBranchFilePath(){
        return Settings.branchFolderPath + getName() + ".txt";
    }


    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != startCommit){
            res.add(currentCommit.toString());
            currentCommit = currentCommit.getPreviousCommit();
        }
        return res;
    }

    List<String> getCommittedState(){
        return commitManager.getCommittedItemsData();
    }

}
