public class Settings {

    private static String currentUser = "Administrator";


    final public static String magitFolder = "/.magit";
    final public static String objectsFolder = magitFolder + "/objects/";
    final public static String branchFolder = magitFolder + "/branches/";
    final public static String remoteBranchFolder = magitFolder + "/remote_branches/";

    final static String activeBranchFileName = "HEAD";
    final private static String activeBranchFile = branchFolder + activeBranchFileName;
    final public static String repositoryDetailsFile = magitFolder + "/repo";
    final public static String repositoryRemoteDetailsFile = magitFolder + "/remote_repo";

    final static String delimiter = ",";
    final static String YNquestion = "Y/N";

    static String repositoryFullPath = "";
    static String objectsFolderPath = "";
    static String branchFolderPath = "";
    static String activeBranchFilePath = "";
    static String repositoryDetailsFilePath = "";
    static String remoteBranchesPath = "";
    static  String repositoryRemoteDetailsFilePath = "";

    final static String gitFolder = ".magit";

    static void setNewRepository(String repositoryPath ){
        repositoryFullPath = repositoryPath ;
        objectsFolderPath = repositoryFullPath+objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
        activeBranchFilePath = repositoryFullPath + activeBranchFile;
        repositoryDetailsFilePath = repositoryFullPath + repositoryDetailsFile;
        remoteBranchesPath = repositoryFullPath + remoteBranchFolder;
        repositoryRemoteDetailsFilePath = repositoryFullPath + repositoryRemoteDetailsFile;
    }

    public static String getUser(){return currentUser;}

    public static void setUser(String user){
        currentUser = user;
    }
}
