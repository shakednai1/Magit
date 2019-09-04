package models;

import core.Branch;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;

public class BranchData {
    private String name;
    private Property<String> headSha1 = new SimpleStringProperty();
    private String headMsg;

    public BranchData(String name, String headSha1, String headMsg) {
        this.name = name;
        this.headSha1.setValue(headSha1);
        this.headMsg = headMsg;
    }

    public BranchData(Branch branch) {
        this.name = branch.getName();
        this.headSha1.setValue(branch.getHead().getSha1());
        this.headMsg = branch.getHead().getMsg();
    }

    public String getName() {
        return name;
    }

    public String getHeadSha1() {
        return headSha1.getValue();
    }

    public Property<String> getHeadSha1Property() {
        return headSha1;
    }

    public void setHeadSha1(String headSha1) {
        this.headSha1.setValue(headSha1);
    }

    public void setHeadMsg(String headMsg) {
        this.headMsg = headMsg;
    }

    @Override
    public String toString() {
        return name + ", "+ headSha1 + ", "+ headMsg;
    }
}