package core;

import jdk.nashorn.internal.objects.annotations.Getter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteRepository {

    public String getName() {
        return name;
    }

    public File getPath() {
        return path;
    }

    public String getOwnerUser() {
        return ownerUser;
    }

    String name;
    File path;
    String ownerUser;

    public List<RemoteBranch> getRemoteBranches() {
        return remoteBranches;
    }

    private List<RemoteBranch> remoteBranches;

    Settings repoSettings;


    RemoteRepository(String name, File path, Settings repoSettings){
        this.repoSettings = repoSettings;

        this.name = name;
        this.path = path;
        this.ownerUser = Settings.getUserFromPath(path.getAbsolutePath());

        loadRemoteBranches();
    }

    RemoteRepository(File remoteSettings, Settings repoSettings){
        this.repoSettings = repoSettings;

        List<String> remoteData = FSUtils.getFileLines(remoteSettings.getAbsolutePath());
        String[] remoteDetails = remoteData.get(0).split(Settings.delimiter);
        name = remoteDetails[1];
        path = new File( remoteDetails[0]);
        ownerUser = Settings.getUserFromPath(path.getAbsolutePath());

        loadRemoteBranches();
    }

    void loadRemoteBranches(){
        remoteBranches = new LinkedList<>();
        File remoteBranchesFolder = new File(repoSettings.remoteBranchesPath);
        for(File branch : remoteBranchesFolder.listFiles()){
            if(!branch.getName().equals("HEAD")){
                String pointedCommit = FSUtils.getFileLines(branch.getPath()).get(0).split(Settings.delimiter)[0];
                RemoteBranch remoteBranch = new RemoteBranch(branch.getName().split(".txt")[0], pointedCommit);
                remoteBranches.add(remoteBranch);
            }
        }
    }

    public RemoteBranch getBranchByName(String name){
        return remoteBranches.stream()
                .filter(b -> b.getName().equals(name))
                .collect(Collectors.toList())
                .get(0);
    }


    void addRemoteBranch(RemoteBranch remoteBranch){
        remoteBranches.add(remoteBranch);
        FSUtils.createNewFile(repoSettings.remoteBranchesPath + remoteBranch.name + ".txt",
                remoteBranch.pointedCommitSha1);
    }

    void updatePointingBranchOfRB(String remoteBranchName){
        String pointingCommitOfRemoteBranch = getPointingCommitOfRB(remoteBranchName);
        RemoteBranch rb = getBranchByName(remoteBranchName);
        rb.pointedCommitSha1 = pointingCommitOfRemoteBranch;
    }

    String getPointingCommitOfRB(String remoteBranchName){
        String branchDataFromFile = FSUtils.getFileLines(getPath().getAbsolutePath() + Settings.branchFolder + remoteBranchName + ".txt").get(0);
        return branchDataFromFile.split(",")[0];
    }


}
