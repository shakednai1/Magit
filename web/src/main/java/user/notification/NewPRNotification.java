package user.notification;


import core.FSUtils;
import core.ItemSha1;
import core.Settings;
import pullRequest.PullRequest;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static user.notification.Notification.NotificationType.NEW_PULL_REQUEST;

public class NewPRNotification extends Notification{

    String operatorRepoName;
    String toBranch;
    String fromBranch;
    String prSHA1;

    public NewPRNotification(String ownerRepoName, String operatorRepoName,
                             String ownerUser, String operatorUser,
                             String toBranch, String fromBranch,
                             String prSHA1){
        super(ownerRepoName, ownerUser, operatorUser, NotificationType.NEW_PULL_REQUEST);

        this.operatorRepoName = operatorRepoName;
        this.toBranch = toBranch;
        this.fromBranch = fromBranch;
        this.prSHA1 = prSHA1;
    }

    public NewPRNotification(PullRequest pr){
        super(pr.getRemoteRepoName(), pr.getOwnerUser(), pr.getRequestingUser(), NotificationType.NEW_PULL_REQUEST);

        this.operatorRepoName = pr.getRepoName();
        this.toBranch = pr.getToBranch();
        this.fromBranch = pr.getFromBranch();
        this.prSHA1 = pr.getSha1().toString();
    }


    public NewPRNotification(File notificationPath) {
        List<String> content = FSUtils.getFileLines(notificationPath.getAbsolutePath());
        String[] fields = content.get(0).split(",");

        time = Long.parseLong(fields[0]);
        repoName = fields[1];
        operatorUser = fields[2];
        ownerUser = Settings.getUserFromPath(notificationPath.getAbsolutePath());
        type = NEW_PULL_REQUEST;

        operatorRepoName = fields[3];
        toBranch = fields[4];
        fromBranch = fields[5];
        prSHA1 = fields[6];

        sha1 = new ItemSha1(notificationPath.getName().split(Pattern.quote("."))[0], false, false, notificationPath.getParentFile());
    }

    public String toString(){
        return String.join(",",
                Long.toString(time),
                repoName,
                operatorUser,
                repoName,
                operatorRepoName,
                toBranch,
                fromBranch,
                prSHA1);
    }

    public String show(){
        return " New pull request for repository '"+ repoName + "'" +
                " from branch '" + fromBranch + "'" +
                " to branch '"+ toBranch + "'";
    }

}
