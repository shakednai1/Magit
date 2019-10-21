package user.notification;

import core.FSUtils;
import core.ItemSha1;
import core.Settings;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class ForkNotification extends Notification {

    public ForkNotification(String repoName, String ownerUser, String operatorUser) {
        super(repoName, ownerUser, operatorUser, NotificationType.FORK);
    }

    public ForkNotification(File notificationPath) {
        List<String> content = FSUtils.getFileLines(notificationPath.getAbsolutePath());
        String[] fields = content.get(0).split(",");

        time = Long.parseLong(fields[0]);
        repoName = fields[1];
        operatorUser = fields[2];
        ownerUser = Settings.getUserFromPath(notificationPath.getAbsolutePath());
        type = NotificationType.FORK;
        sha1 = new ItemSha1(notificationPath.getName().split(Pattern.quote("."))[0], false, false, notificationPath.getParentFile());
    }

    @Override
    public String toString(){
        return String.join(Settings.delimiter, Long.toString(time), repoName, operatorUser);
    }

    public String show(){
        return "User " + operatorUser +
            " forked repository " + repoName +
            " at time " + new Date(time).toString();
    }

}