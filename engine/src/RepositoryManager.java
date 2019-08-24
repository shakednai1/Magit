import java.io.File;

class RepositoryManager {

    private Repository activeRepository = null;


    Repository getActiveRepository(){
        return activeRepository;
    }

    void createNewRepository(String repositoryFullPath, String name, boolean empty){
        Settings.setNewRepository(repositoryFullPath);

        File objectsPath = new File(Settings.objectsFolderPath);
        if(!objectsPath.exists())
            objectsPath.mkdirs();

        File branchesPath = new File(Settings.branchFolderPath);
        if(!branchesPath.exists())
            branchesPath.mkdirs();

        Repository repo = new Repository(repositoryFullPath, name, empty);
        activeRepository = repo;
    }

    void switchActiveRepository(String fullPath){
        if(!verifyRepoPath(fullPath)){
            throw new IllegalArgumentException();
        }

        Settings.setNewRepository(fullPath);
        activeRepository = Repository.load(fullPath);
    }

    private boolean verifyRepoPath(String fullPath) {
        File directory = new File(fullPath + "\\.magit");
        return directory.exists();
    }


}
