public class Settings {

    static String delimiter = ",";
    private static String objectsFolder = "/.magit/.objects/";
    private static String branchFolder = "/.magit/.branches/";

    static String repositoryFullPath = "";
    static String objectsFolderPath = "";
    static String branchFolderPath = "";

    static String gitFolder = ".magit";

    static void setNewRepository(String repositoryPath ){
        repositoryFullPath = repositoryPath ;
        objectsFolderPath = repositoryFullPath+objectsFolder;
        branchFolderPath = repositoryFullPath + branchFolder;
    }
}
