package user.notification;

import core.FSUtils;
import core.ItemSha1;
import core.Settings;
import pullRequest.PullRequest;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import static user.notification.Notification.NotificationType.EDIT_PULL_REQUEST;

public class EditPRNotification extends  NewPRNotification{

    String status;

    public EditPRNotification(PullRequest pr){
        super(pr);
        type = EDIT_PULL_REQUEST;

        status  = pr.getStatus().name();
    }

    public EditPRNotification(File notificationPath) {
        super(notificationPath);

        type = EDIT_PULL_REQUEST;

        List<String> content = FSUtils.getFileLines(notificationPath.getAbsolutePath());
        String[] fields = content.get(0).split(",");

        status = fields[7];
    }

    @Override
    File getNotificationFolder(){
        File operatorPath = Settings.getUserPath(this.operatorUser);
        return Settings.getNotificationFolder(operatorPath, type.name());
    }

    public String toString(){
        return super.toString() + Settings.delimiter + status;
    }

    public String show(){
        return " Pull request for repository '"+ repoName + "'" +
                " from branch '" + fromBranch + "'" +
                " to branch '"+ toBranch + "' " +
                status;
    }

}
