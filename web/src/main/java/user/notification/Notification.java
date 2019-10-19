package user.notification;

import core.ItemSha1;
import core.Settings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public abstract class Notification {
    static enum NotificationType {FORK, NEW_PULL_REQUEST, EDIT_PULL_REQUEST};

    long time;
    String repoName;
    String ownerUser;
    String operatorUser;

    NotificationType type;
    ItemSha1 sha1;

    Settings repoSettings;

    Notification(String repoName, String ownerUser, String operatorUser, NotificationType type,
                 Settings repoSettings){
        this.repoName = repoName;
        this.ownerUser = ownerUser;
        this.operatorUser = operatorUser;
        this.type = type;
        this.repoSettings = repoSettings;
        this.time = new Date().getTime();
        setSha1();
    }

    private void setSha1(){
        sha1 = new ItemSha1(toString(), true, false, repoSettings);
    }

    public abstract String toString();

    public void save(){
        String ownerRepoPath = Settings.getRepoPathByUser(ownerUser, repoName);
        String objStr = serialize();
        File notificationPath = Settings.getNotificationPath(ownerRepoPath, sha1.toString());

        try {
            FileUtils.writeStringToFile(notificationPath, objStr, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected String serialize(){
        return sha1.toString();
    }
    protected abstract Object deserialize();
}
