package user;

import core.MainEngine;
import core.Settings;
import user.notification.ForkNotification;
import user.notification.Notification;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class User {

    String name;
    String password;

    MainEngine engine;

    ArrayList<String> repos = new ArrayList();

    public String getName() {
        return name;
    }

    public ArrayList getRepos() {
        return repos;
    }

    User(String name, String password){
        this.name = name;
        this.password = password;

        this.engine = new MainEngine(this.name);
    }

    public void addRepo(String repoName){
        repos.add(repoName);
    }

    public MainEngine getEngine(){
        return engine;
    }

    public List<Notification> getNotifications(boolean reset, boolean sort){
        List<Notification> notifications = new ArrayList<>();

        Map<Notification.NotificationType, List<File>> notificationsSHA1 = getNotificationSHA1();

        for(Notification.NotificationType type: notificationsSHA1.keySet()){
            if(type == Notification.NotificationType.FORK){
                for(File notificationPath: notificationsSHA1.get(type))
                    notifications.add(new ForkNotification(notificationPath));
            }

        }

        if (reset){
            for(Notification notification: notifications)
                notification.delete();
        }

        if(sort){
            notifications = notifications.stream().sorted().collect(Collectors.toList());
        }

        return notifications;

    }

    private Map<Notification.NotificationType, List<File>> getNotificationSHA1(){

        Map<Notification.NotificationType, List<File >> notificationsSHA1 = new HashMap<>();

        File ownerPath = Settings.getUserPath(name);

        for(Notification.NotificationType type: Notification.NotificationType.values()){
            File typeFolder = Settings.getNotificationFolder(ownerPath, type.name());

            List<File> notificationFiles = new ArrayList<>();

            File[] files = typeFolder.listFiles();
            if (files != null)
                notificationFiles.addAll(Arrays.stream(files).collect(Collectors.toList()));

            notificationsSHA1.put(type, notificationFiles);
        }

        return notificationsSHA1;
    }


}

