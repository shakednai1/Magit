import java.util.LinkedList;
import java.util.List;

class Branch {

    private Commit startCommit; //TODO add load commit - do not change it after define it
    private Commit head;
    private String name;


    Branch(String name, Commit head){
        this.name = name;
        this.head = head;
        startCommit = head;
    }

    Commit getHead(){
        return head;
    }

    String getName(){
        return name;
    }

    void setHead(Commit newHead) {
        head = newHead;
        Utils.deleteFile(Settings.branchFolderPath + name + ".txt");
        Utils.createNewFile(Settings.branchFolderPath + name + ".txt", newHead.commitSha1);
    }

    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != startCommit){
            res.add(currentCommit.commitSha1 + Settings.delimiter + currentCommit.msg + Settings.delimiter +
                    currentCommit.commitTime + Settings.delimiter + currentCommit.rootFolder.userLastModified);
            currentCommit = currentCommit.previousCommit;
        }
        return res;
    }
}
