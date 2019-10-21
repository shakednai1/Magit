package user.notification;

import core.ItemSha1;
import core.Settings;
import org.apache.commons.io.FileUtils;

import javax.tools.DiagnosticListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public abstract class Notification implements Comparable<Notification> {
    public static enum NotificationType {FORK, NEW_PULL_REQUEST, EDIT_PULL_REQUEST};

    long time;
    String repoName;
    String ownerUser;
    String operatorUser;

    NotificationType type;
    ItemSha1 sha1;

    Notification(){}

    Notification(String repoName, String ownerUser, String operatorUser, NotificationType type){
        this.repoName = repoName;
        this.ownerUser = ownerUser;
        this.operatorUser = operatorUser;
        this.type = type;
        this.time = new Date().getTime();
        setSha1();
    }

    public abstract String toString();

    public NotificationType getType(){ return type; }

    private void setSha1(){
        sha1 = new ItemSha1(toString(), true, false, getNotificationFolder());
    }

    File getNotificationFolder(){
        File ownerPath = Settings.getUserPath(ownerUser);
        return Settings.getNotificationFolder(ownerPath, type.name());
    }

    public void save(){
        File notificationPath = Settings.getNotificationPath(getNotificationFolder(), sha1.toString());

        try {
            FileUtils.writeStringToFile(notificationPath, serialize(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(){
        File notificationPath = Settings.getNotificationPath(getNotificationFolder(), sha1.toString());
        notificationPath.delete();
    }

    protected String serialize(){
        return toString();
    }

    public abstract String show();

    @Override
    public int compareTo(Notification other){
        if(this.time == other.time)
            return 0;
        if (this.time < other.time)
            return 1;
        return -1;
    }

}
