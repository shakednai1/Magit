package models;

import core.Branch;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BranchData {
    private String name;
    private StringProperty headSha1 = new SimpleStringProperty();
    private String headMsg;
    private String trackingAfter;


    public BranchData(String name, String headSha1, String headMsg, String trackingAfter) {
        this.name = name;
        this.headSha1.setValue(headSha1);
        this.headMsg = headMsg;
        this.trackingAfter = trackingAfter;
    }

    public BranchData(Branch branch) {
        this.name = branch.getName();
        this.headSha1.setValue(branch.getHead().getSha1());
        this.headMsg = branch.getHead().getMsg();
        this.trackingAfter = branch.getTrackingAfter();
    }

    public String getName() {
        return name;
    }

    public String getHeadSha1() {
        return headSha1.getValue();
    }

    public StringProperty getHeadSha1Property() {
        return headSha1;
    }

    public void setHeadSha1(String headSha1) {
        this.headSha1.setValue(headSha1);
    }

    public void setHeadMsg(String headMsg) {
        this.headMsg = headMsg;
    }

    public String getTrackingAfter(){return this.trackingAfter; }
    public void setTrackingAfter(String trackingAfter){this.trackingAfter = trackingAfter; }


    @Override
    public String toString() {
        return name + ", "+ headSha1.toString() + ", "+ headMsg;
    }
}