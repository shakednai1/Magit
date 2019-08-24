package models;

public class BranchData {
    private String name;
    private String headSha1;
    private String headMsg;

    public BranchData(String name, String headSha1, String headMsg) {
        this.name = name;
        this.headSha1 = headSha1;
        this.headMsg = headMsg;
    }

    public String getName() {
        return name;
    }

    public String getHeadSha1() {
        return headSha1;
    }

    public void setHeadSha1(String headSha1) {
        this.headSha1 = headSha1;
    }

    public void setHeadMsg(String headMsg) {
        this.headMsg = headMsg;
    }

    @Override
    public String toString() {
        return name + ", "+ headSha1 + ", "+ headMsg;
    }
}