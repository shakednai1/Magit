package user.notification;

import java.io.File;

public class NotificationFactory {

    public static Notification getNotification(File notificationPath, Notification.NotificationType notificationType){

        if(notificationType == Notification.NotificationType.FORK){
            return new ForkNotification(notificationPath);
        }
        return null;

    }
}
