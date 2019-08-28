import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Settings {

    final public static DateFormat commitDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");

    final private static String magitFolder = "/.magit";
    final private static String objectsFolder = magitFolder + "/objects/";
    final private static String branchFolder = magitFolder + "/branches/";

    final static String activeBranchFileName = "HEAD";
    final private static String activeBranchFile = branchFolder + activeBranchFileName;
    final private static String repositoryDetailsFile = magitFolder + "/repo";

    final static String delimiter = ",";
    final static String YNquestion = "Y/N";

    static String repositoryFullPath = "";
    static String objectsFolderPath = "";
    static String branchFolderPath = "";
    static String activeBranchFilePath = "";
    static String repositoryDetailsFilePath = "";

    final static String gitFolder = ".magit";


    private static String currentUser = "Administrator";

    static void setNewRepository(String repositoryPath ){
        repositoryFullPath = repositoryPath ;
        objectsFolderPath = repositoryFullPath+objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
        activeBranchFilePath = repositoryFullPath + activeBranchFile;
        repositoryDetailsFilePath = repositoryFullPath + repositoryDetailsFile;
    }

    public static String getUser(){return currentUser;}

    public static void setUser(String user){
        currentUser = user;
    }
}
