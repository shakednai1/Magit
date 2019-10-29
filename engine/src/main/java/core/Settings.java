package core;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Settings {

    static boolean webMode = true;
    static File runningPath;

    final public static File baseLocation = new File("c:", "magit-ex3");

    final public static DateFormat commitDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");

    final public static String magitFolder = "/.magit";
    final public static String objectsFolder = magitFolder + "/objects/";
    final public static String branchFolder = magitFolder + "/branches/";
    final public static String remoteBranchFolder = magitFolder + "/remote_branches/";
    final public static String pullRequestFolder = magitFolder + "/pull_requests/";
    final public static String notificationFolder = ".notifications";


    final static String activeBranchFileName = "HEAD";
    final public static String activeBranchFile = branchFolder + activeBranchFileName;
    final public static String repositoryDetailsFile = magitFolder + "/repo";
    final public static String repositoryRemoteDetailsFile = magitFolder + "/remote_repo";

    final public static String delimiter = ",";
    final public static String YNquestion = "Y/N";

    String userName;
    String repositoryName;
    String repositoryFullPath = "";
    String objectsFolderPath = "";
    String branchFolderPath = "";
    String activeBranchFilePath = "";
    String repositoryDetailsFilePath = "";
    String remoteBranchesPath = "";
    String repositoryRemoteDetailsFilePath = "";

    final static String gitFolder = ".magit";

    Settings(String userName){
        this.userName = userName;
    }

    public String getUser(){ return userName; }
    void setUser(String userName){
        // DO NOT USE IN WEB MODE
        this.userName = userName;
    }

    public String getRepositoryFullPath(){ return repositoryFullPath; }

    public File getRepositoryObjectsFullPath(){ return new File(objectsFolderPath); }

    void setNewRepository(String repository ){
        // repository param : in web mode , repository = repositoryName
        // else: repository = repositoryPath
        if(webMode){
            File repoFile =  new File(baseLocation, userName);
            repoFile = new File(repoFile, repository);
            repoFile.mkdirs();

            repositoryName = repository;
            repositoryFullPath = repoFile.getAbsolutePath();
        }
        else {
            repositoryFullPath = repository ;
        }

        objectsFolderPath = repositoryFullPath + objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
        activeBranchFilePath = repositoryFullPath + activeBranchFile;
        repositoryDetailsFilePath = repositoryFullPath + repositoryDetailsFile;
        remoteBranchesPath = repositoryFullPath + remoteBranchFolder;
        repositoryRemoteDetailsFilePath = repositoryFullPath + repositoryRemoteDetailsFile;
    }

    public static void setRunningPath(File path){
        runningPath = path;
    }

    public String getRepoPathByCurrentUser(String repoName){
        return getRepoPathByUser(userName, repoName);
    }

    public static String getRepoPathByUser(String userName, String repoName){
        File repoFile =  new File(baseLocation, userName);
        repoFile = new File(repoFile, repoName);
        return repoFile.getAbsolutePath();
    }

    public static File getBranchFolderByRepo(File repoPath){
        return new File(repoPath, branchFolder);
    }

    public static File getPullRequestFolder(String userName, String repoName){
        File repoFile = new File(getRepoPathByUser(userName, repoName));
        return new File(repoFile, pullRequestFolder);
    }

    public String getBranchFilePath(String branchName){
        return branchFolderPath + branchName + ".txt";
    }

    public static File getUserPath(String userName){
        return new File(baseLocation, userName);
    }

    public static File getNotificationPath(File notificationFolder, String notificationSha1){
        return new File(notificationFolder, notificationSha1 + ".txt");
    }

    public static File getNotificationFolder(File userPath, String notificationType){
        return new File(new File(userPath, notificationFolder), notificationType);
    }

    public static String getUserFromPath(String path) throws ValueException{
        if(!path.startsWith(baseLocation.getAbsolutePath()))
            throw new ValueException("Not magit path");

        return path.split(Pattern.quote("\\"))[2];
    }

    public static String buildRepoFilePath(String user, String repoName, String fileName){
        String repoFolder = getRepoPathByUser(user, repoName);

        return new File(repoFolder, fileName).getAbsolutePath();
    }

    public String extractFilePath(String fullPath) {

        String repoFolder = getRepoPathByUser(userName, repositoryName);
        File _path = new File(fullPath);

        String filePath = _path.getName();
        _path = _path.getParentFile();
        while(!_path.getAbsolutePath().equals(repoFolder))  {
            filePath = _path.getName() + "\\" +filePath;
            _path = _path.getParentFile();
        }

        return filePath;

    }
}
