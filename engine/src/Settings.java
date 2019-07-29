

public class Settings {

    private static String currentUser = "Administrator";

    final static String delimiter = ",";
    final private static String magitFolder = "/.magit";
    final private static String objectsFolder = magitFolder + "/.objects/";
    final private static String branchFolder = magitFolder + "/.branches/";
    final private static String activeBranchFile = branchFolder + "HEAD";

    static String repositoryFullPath = "";
    static String objectsFolderPath = "";
    static String branchFolderPath = "";
    static String activeBranchFilePath = "";

    final static String gitFolder = ".magit";

    static void setNewRepository(String repositoryPath ){
        repositoryFullPath = repositoryPath ;
        objectsFolderPath = repositoryFullPath+objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
        activeBranchFilePath = repositoryFullPath + activeBranchFile;
    }

    static String getUser(){return currentUser;}

    static void setUser(String user){
        currentUser = user;
    }
}
