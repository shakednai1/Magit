package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Settings {

    static boolean webMode = true;
    static File runningPath;

    final public static File baseLocation = new File("c:", "magit-ex3");

    final public static DateFormat commitDateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");

    final public static String magitFolder = "/.magit";
    final public static String objectsFolder = magitFolder + "/objects/";
    final public static String branchFolder = magitFolder + "/branches/";
    final public static String remoteBranchFolder = magitFolder + "/remote_branches/";

    final static String activeBranchFileName = "HEAD";
    final public static String activeBranchFile = branchFolder + activeBranchFileName;
    final public static String repositoryDetailsFile = magitFolder + "/repo";
    final public static String repositoryRemoteDetailsFile = magitFolder + "/remote_repo";

    final public static String delimiter = ",";
    final public static String YNquestion = "Y/N";

    String userName;
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

    String getUser(){ return userName; }
    void setUser(String userName){
        // DO NOT USE IN WEB MODE
        this.userName = userName;
    }


    void setNewRepository(String repository ){
        // repository param : in web mode , repository = repositoryName
        // else: repository = repositoryPath
        if(webMode){
            File repoFile =  new File(baseLocation, userName);
            repoFile = new File(repoFile, repository);
            repoFile.mkdirs();

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
        File repoFile =  new File(baseLocation, userName);
        repoFile = new File(repoFile, repoName);
        return repoFile.getAbsolutePath();
    }

    String getBranchFilePath(String branchName){
        return branchFolderPath + branchName + ".txt";
    }
}
