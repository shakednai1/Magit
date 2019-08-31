package core;

public class RemoteBranch {

    String name;
    String pointedCommitSha1;

    public RemoteBranch(String name, String pointedCommitSha1){
        this.name = name;
        this.pointedCommitSha1 = pointedCommitSha1;
    }

    public String getName(){
        return name;
    }

}
