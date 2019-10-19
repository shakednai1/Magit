package user.notification;

import core.Settings;

public class ForkNotification extends Notification {
    public ForkNotification(String repoName, String ownerUser, String operatorUser, Settings repoSettings) {
        super(repoName, ownerUser, operatorUser, NotificationType.FORK, repoSettings);
    }

    @Override
    public String toString(){
        return String.join(Settings.delimiter, Long.toString(time), repoName, operatorUser);
    }

    protected Object deserialize(){return null;}

}