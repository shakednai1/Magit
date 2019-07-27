import java.util.LinkedList;
import java.util.List;

public class Branch {

    private Commit startCommit; //TODO add start commit - do not change it after define it
    private Commit head;
    private String name;


    public Branch(String name, Commit head){
        this.name = name;
        this.head = head;
        startCommit = head;
    }

    public Commit getHead(){
        return head;
    }

    public String getName(){
        return name;
    }

    public void setHead(Commit newHead) {
        head = newHead;
        Utils.deleteFile(RepositoryManager.getActiveRepository().getBranchesFolderPath() + name + ".txt");
        Utils.createNewFile(RepositoryManager.getActiveRepository().getBranchesFolderPath() + name + ".txt", newHead.commitSha1);
    }

    public List<String> getCommitHistory(){
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
