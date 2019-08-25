import org.apache.commons.io.FileUtils;

import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

class RepositoryManager {

    private Repository activeRepository = null;

    // TODO - delete content of .branchs and .objects - do not keep the state, faster dir scanner

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

    public void cloneRepository(String sourcePath, String destPath, String repoName){
        List<String> lines = Utils.getFileLines(sourcePath + "/.magit/repo");
        String remoteRepoName = lines.get(0);
        File srcDir = new File(sourcePath);
        File destDir = new File(destPath);
        try {
            FileUtils.copyDirectory(srcDir,destDir);
        }
        catch (IOException e){
        }
        List<RemoteBranch> remoteBranches = new LinkedList<>();

        File branchesDir = new File(destDir + "/.magit/branches");
        for (File branch : branchesDir.listFiles()){
            if(!branch.getName().equals("HEAD")){
                List<String> branchLines = Utils.getFileLines(branch.getPath());
                remoteBranches.add(new RemoteBranch(remoteRepoName + "/" + branch.getName(), branchLines.get(0)));
            }
        }

        for(File file : branchesDir.listFiles()){
            file.delete();
        }
        createNewRepository(destPath, repoName, false);
        //switchActiveRepository(destPath);
        for(RemoteBranch remoteBranch : remoteBranches){
            activeRepository.addRemoteBranch(remoteBranch);
        }
        // TODO fix open change while clone
        activeRepository.setRemoteRepositoyPath(sourcePath);
        activeRepository.setRemoteRepositoyName(remoteRepoName);

    }


}
