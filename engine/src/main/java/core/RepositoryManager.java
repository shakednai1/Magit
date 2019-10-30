package core;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RepositoryManager {

    Settings settings;
    private Repository activeRepository = null;

    RepositoryManager(String userName){

        settings = new Settings(userName);
    }

    Repository getActiveRepository(){
        return activeRepository;
    }

    public Settings getSettings() {return settings;}

    void createNewRepository(String repositoryFullPath, String name, boolean empty){
        settings.setNewRepository(settings.webMode? name: repositoryFullPath);

        File objectsPath = new File(settings.objectsFolderPath);
        if(!objectsPath.exists())
            objectsPath.mkdirs();

        File branchesPath = new File(settings.branchFolderPath);
        if(!branchesPath.exists())
            branchesPath.mkdirs();

        Repository repo = new Repository(settings.repositoryFullPath, name, empty, settings);
        activeRepository = repo;
    }

    void switchActiveRepository(String repository){
        if(!Settings.webMode){
            if(!verifyRepoPath(repository)){
                throw new IllegalArgumentException();
            }
        }

        settings.setNewRepository(repository);
        activeRepository = Repository.load(settings);
    }

    private boolean verifyRepoPath(String fullPath) {
        File directory = new File(fullPath + "\\.magit");
        return directory.exists();
    }

    public void cloneRepository(String sourcePath, String destPath, String repoName){
        String branchesPath = destPath + Settings.branchFolder;
        String remoteBranchesPath = destPath + Settings.remoteBranchFolder;

        // extract remote repo name
        String remoteRepoName = FSUtils.getFileLines(sourcePath + Settings.repositoryDetailsFile).get(0);

        // copy remote repo to local repo
        File srcDir = new File(sourcePath);
        File destDir = new File(destPath);
        try {
            FileUtils.copyDirectory(srcDir,destDir);

            File copiesSrcPullRequests = new File(destDir ,Settings.pullRequestFolder);
            FileUtils.deleteDirectory(copiesSrcPullRequests);
        }
        catch (IOException e){
        }

        // copy all branches files to remote branches folder
        copyBranchesFromRemote(branchesPath, remoteBranchesPath);
        File branchesDir = new File(branchesPath);
        for(File file: branchesDir.listFiles()){
            file.delete();
        }

        // update repo file name
        FSUtils.writeFile(destDir + Settings.repositoryDetailsFile, repoName, false);
        FSUtils.writeFile(destDir + Settings.repositoryRemoteDetailsFile, sourcePath + Settings.delimiter + remoteRepoName, false);

        createNewBranchFilsTrackingAfter(remoteBranchesPath, branchesPath);
        switchActiveRepository(repoName);
        getActiveRepository().setBranchesTrackingAfterSameName();
    }

    public void createNewBranchFilsTrackingAfter(String remoteBranchesPath, String branchesPath){
        // create new branch pointing to the current commit
        String branchName = FSUtils.getFileLines(remoteBranchesPath + "HEAD").get(0);
        String headRemotePointedCommit = FSUtils.getFileLines( remoteBranchesPath + branchName + ".txt").get(0);
        String content = headRemotePointedCommit + Settings.delimiter + branchName;

        // create branch file + update head
        FSUtils.createNewFile(branchesPath + branchName + ".txt", content);
        FSUtils.createNewFile(branchesPath + "HEAD", branchName);
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
