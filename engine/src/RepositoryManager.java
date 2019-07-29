import java.io.File;
import java.util.HashMap;
import java.util.Map;

class RepositoryManager {

    private Map<String, Repository> repositories = new HashMap<>();
    private Repository activeRepository = null;

    // TODO - delete content of .branchs and .objects - do not keep the state, faster dir scanner

    Repository getActiveRepository(){
        return activeRepository;
    }

    Repository createNewRepository(String repositoryFullPath){
        Repository newRepository = new Repository(repositoryFullPath);
        repositories.put(repositoryFullPath, newRepository);
        return newRepository;
    }

    void switchActiveRepository(String fullPath){
        if(!verifyRepoPath(fullPath)){
            throw new IllegalArgumentException();
        }

        Settings.setNewRepository(fullPath);

        Repository rep = repositories.get(fullPath);
        if (rep != null){
            activeRepository = rep;
            return;
        }

        activeRepository = createNewRepository(fullPath);
    }

    private boolean verifyRepoPath(String fullPath) {
        File directory = new File(fullPath + "\\.magit");
        return directory.exists();
    }

    void saveState(){
        activeRepository.saveRepositoryActiveBranch();
    }

}
