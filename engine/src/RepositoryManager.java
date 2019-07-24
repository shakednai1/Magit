import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RepositoryManager {

    private static List<Repository> repositories = new ArrayList<>();
    private static Repository activeRepository = null;


    public static Repository getActiveRepository(){
        return activeRepository;
    }

    public static List<Repository> getAllRepositories(){
        return repositories;
    }

    public static void addToRepositoriesList(Repository repository){
        repositories.add(repository);
    }

    public static void switchActiveRepository(String fullPath){
        for(Repository repository: repositories){
            if (repository.getFullPath().equals(fullPath)){
                activeRepository = repository;
                return;
            }
        }
        if(verifyRepoPath(fullPath)){
            Repository newRepository = new Repository(fullPath);
            addToRepositoriesList(newRepository);
            activeRepository = newRepository;
            activeRepository.getCommitManager().start();
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    private static boolean verifyRepoPath(String fullPath) {
        File directory = new File(fullPath + "\\.magit");
        return directory.exists();
    }

}
