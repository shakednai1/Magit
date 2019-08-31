import exceptions.InvalidBranchNameError;
import exceptions.UncommittedChangesError;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    public void cloneRepository(String sourcePath, String destPath, String repoName, boolean rtbBranch){
        String branchesPath = destPath + Settings.branchFolder;
        String remoteBranchesPath = destPath + Settings.remoteBranchFolder;

        // extract remote repo name
        String remoteRepoName = Utils.getFileLines(sourcePath + Settings.repositoryDetailsFile).get(0);

        // copy remote repo to local repo
        File srcDir = new File(sourcePath);
        File destDir = new File(destPath);
        try {
            FileUtils.copyDirectory(srcDir,destDir);
        }
        catch (IOException e){
        }

        // copy all branches files to remote branches folder
        copyBranchesFromRemote(branchesPath, remoteBranchesPath);
        File branchesDir = new File(branchesPath);
        for(File file: branchesDir.listFiles()){
            file.delete();
        }
        // create new branch pointing to the current commit
        // if the user wants to create rtb - create rtb to the head branch
        String branchName = Utils.getFileLines(remoteBranchesPath + "HEAD").get(0);
        String headRemotePointedCommit = Utils.getFileLines( remoteBranchesPath + branchName + ".txt").get(0);
        String content = headRemotePointedCommit + Settings.delimiter + branchName;

        if(!rtbBranch){
            branchName = "master";
            content = headRemotePointedCommit + ",null";
        }

        // create branch file + update head
        Utils.createNewFile(branchesPath + branchName + ".txt", content);
        Utils.createNewFile(branchesPath + "HEAD", branchName);

        // update repo file name
        Utils.writeFile(destDir + Settings.repositoryDetailsFile, repoName, false);
        Utils.writeFile(destDir + Settings.repositoryRemoteDetailsFile, sourcePath + Settings.delimiter + remoteRepoName, false);

        switchActiveRepository(destPath);

        activeRepository.getActiveBranch().addTracking(branchName);
        activeRepository.getActiveBranch().writeBranchInfoFile();
        activeRepository.setRemoteRepositoryPath(sourcePath);
        activeRepository.setRemoteRepositoryName(remoteRepoName);

    }

    public void copyBranchesFromRemote(String branchesPath, String remoteBranchesPath){
        File branchesDir = new File(branchesPath);
        File remoteBranchesDir = new File(remoteBranchesPath);
        try {
            FileUtils.copyDirectory(branchesDir,remoteBranchesDir);
        }
        catch (IOException e){
        }

    }
}
