import java.io.File;
import java.util.HashMap;
import java.util.Map;

class RepositoryManager {

    private static Map<String, Repository> repositories = new HashMap<>();
    private static Repository activeRepository = null;

    // TODO - delete content of .branchs and .objects - do not keep the state, faster dir scanner

    static Repository getActiveRepository(){
        return activeRepository;
    }

    public static void createNewRepository(String repositoryFullPath){
        Repository newRepository = new Repository(repositoryFullPath);
        repositories.put(repositoryFullPath, newRepository);
        activeRepository = newRepository;
    }

    static void switchActiveRepository(String fullPath){
        if(!verifyRepoPath(fullPath)){
            throw new IllegalArgumentException();
        }

        Settings.setNewRepository(fullPath);

        Repository rep = repositories.get(fullPath);
        if (rep != null){
            activeRepository = rep;
            return;
        }

        createNewRepository(fullPath);
    }

    private static boolean verifyRepoPath(String fullPath) {
        File directory = new File(fullPath + "\\.magit");
        return directory.exists();
    }

}
