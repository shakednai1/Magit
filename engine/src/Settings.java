public class Settings {

    private static String currentUser = "Administrator";


    final private static String magitFolder = "/.magit";
    final private static String objectsFolder = magitFolder + "/objects/";
    final private static String branchFolder = magitFolder + "/branches/";

    final static String activeBranchFileName = "HEAD";
    final private static String activeBranchFile = branchFolder + activeBranchFileName;
    final private static String repositoryDetailsFile = magitFolder + "/repo";

    final static String delimiter = ",";

    static String repositoryFullPath = "";
    static String objectsFolderPath = "";
    static String branchFolderPath = "";
    static String activeBranchFilePath = "";
    static String repositoryDetailsFilePath = "";

    final static String gitFolder = ".magit";

    static void setNewRepository(String repositoryPath ){
        repositoryFullPath = repositoryPath ;
        objectsFolderPath = repositoryFullPath+objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
        activeBranchFilePath = repositoryFullPath + activeBranchFile;
        repositoryDetailsFilePath = repositoryFullPath + repositoryDetailsFile;
    }

    static String getUser(){return currentUser;}

    static void setUser(String user){
        currentUser = user;
    }
}
