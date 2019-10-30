package pullRequest;

import com.google.gson.JsonObject;
import core.*;
import models.BranchData;
import org.apache.commons.io.FileUtils;
import user.UserManager;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class PullRequest implements Comparable<PullRequest> {

    public String getRepoName() {
        return repoName;
    }

    public String getRemoteRepoName() {
        return remoteRepoName;
    }

    public String getRequestingUser() {
        return requestingUser;
    }

    public String getOwnerUser() {
        return ownerUser;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public String getToBranch() {
        return toBranch;
    }

    public PRStatus getStatus(){ return status; }

    public void setStatusAndReason(PRStatus status, String reason) {
        this.status = status;
        this.reason = reason;
        writeStateToFile();
    }

    public ItemSha1 getSha1() {
        return sha1;
    }


    String repoName;
    String remoteRepoName;
    String requestingUser;
    String ownerUser;
    String comment;
    String fromBranch;
    String fromCommitSha1;
    String toBranch;
    String toCommitSha1;
    Date creationTime;
    PRStatus status;
    ItemSha1 sha1 ;
    String reason;


    List<JsonObject> newFiles = new ArrayList<>();
    List<JsonObject> updateFiles = new ArrayList<>();
    List<JsonObject> deleteFiles = new ArrayList<>();


    public static enum PRStatus {NEW, ACCEPTED, DECLINED};

    public PullRequest(String repoName,
                       String remoteRepoName,
                String requestingUser,
                String ownerUser,
                String comment,
                BranchData fromBranch,
                RemoteBranch toBranch){
        this.repoName = repoName;
        this.remoteRepoName = remoteRepoName;
        this.requestingUser = requestingUser;
        this.ownerUser = ownerUser;
        this.comment = comment;
        this.fromBranch = fromBranch.getName();
        this.fromCommitSha1 = fromBranch.getHeadSha1();
        this.toBranch = toBranch.getName();
        this.toCommitSha1 = toBranch.getPointedCommitSha1();

        this.creationTime = new Date();
        status = PRStatus.NEW;

        sha1 = new ItemSha1(UserManager.generatePRNumber().toString(),
                false, false, getPullRequestFolder());
    }


    public PullRequest(String ownerUser, String remoteRepoName, String sha1){
        this.ownerUser = ownerUser;
        this.remoteRepoName = remoteRepoName;
        setSha1(sha1);

        setDetailsFromFile();
        setStateFromFile();
    }


    private void setSha1(String sha1){
        this.sha1 = new ItemSha1(sha1, false, false, getPullRequestFolder());
    }

    private void setDetailsFromFile(){

        List<String> content = FSUtils.getFileLines(getPRDetailsFile().getAbsolutePath());
        String[] fields = content.get(0).split(",");

        requestingUser = fields[0];

        creationTime = new Date();
        creationTime.setTime(Long.parseLong(fields[1]));

        repoName = fields[2];

        fromBranch = fields[3];
        fromCommitSha1 = fields[4];
        toBranch = fields[5];
        toCommitSha1 = fields[6];
        comment = String.join( Settings.delimiter, Arrays.copyOfRange(fields, 7, fields.length));
        
    }

    private void setStateFromFile(){
        List<String> content = FSUtils.getFileLines(getPRStateFile().getAbsolutePath());
        String[] data = content.get(0).split(Settings.delimiter);
        status = PRStatus.valueOf(data[0]);
        reason = String.join( Settings.delimiter, Arrays.copyOfRange(data, 1, data.length));
    }

    private void writeStateToFile(){
        String stateFile = getPRStateFile().getAbsolutePath();
        FSUtils.writeFile(stateFile, status.name() + Settings.delimiter + this.reason, false);
    }

    private File getPullRequestFolder(){
        return Settings.getPullRequestFolder(ownerUser, remoteRepoName);
    }

    private File getCurrentPullRequestFolder(){
        File prFolder = getPullRequestFolder();
        return new File(prFolder, sha1.toString());
    }

    private File getPRDetailsFile(){
        return new File(getCurrentPullRequestFolder(), "details.txt");
    }

    private File getPRStateFile(){
        return new File(getCurrentPullRequestFolder(), "state.txt");
    }

    public void create() throws IOException {
        String objStr = serialize();
        FileUtils.writeStringToFile(getPRDetailsFile(), objStr, "utf-8");
        FileUtils.writeStringToFile(getPRStateFile(), status.toString(), "utf-8");
    }

    String serialize(){
        // TODO - do we need to save sha1 of branches??
        return String.join(Settings.delimiter,
                requestingUser,
                Long.toString(creationTime.getTime()),
                repoName,
                fromBranch,
                fromCommitSha1,
                toBranch,
                toCommitSha1,
                comment);
    }

    public static List<PullRequest> getRepoPullRequests(String ownerUser, String repoName){
        File pullRequestsFolder = Settings.getPullRequestFolder(ownerUser, repoName);
        List<PullRequest> res = new ArrayList<>();

        if(pullRequestsFolder.exists()){
            for(File prSha1: pullRequestsFolder.listFiles()){
                res.add(new PullRequest(ownerUser, repoName, prSha1.getName()));
            }
        }
        return res;
    }

    public void setFileChanges(Repository repository){
        setFilesDeltaCommit(fromCommitSha1, toCommitSha1, repository.getSettings());
    }

    public void setFilesDeltaCommit(String commitSha1, String prevCommit, Settings settings){
        FolderChanges folderChanges = CommitsDelta.getDiffBetweenCommits(commitSha1, prevCommit, settings);
        getFilesState(folderChanges, settings);
    }


    public void getFilesState(FolderChanges folder, Settings settings){
        for(FileChanges file : folder.getSubChangesFiles().values()){
            Common.FilesStatus status = file.getState();
            String path = settings.extractFilePath(file.getFullPath());
            String fileSha1 = file.getSha1();

            JsonObject fileJson = new JsonObject();
            fileJson.addProperty("path", path);
            fileJson.addProperty("sha1", fileSha1);

            if(status == Common.FilesStatus.NEW) {
                newFiles.add(fileJson);
            }
            else if(status == Common.FilesStatus.UPDATED) {
                updateFiles.add(fileJson);
            }
            else if(status == Common.FilesStatus.DELETED) {
                deleteFiles.add(fileJson);
            }
        }
        for(FolderChanges subFolder: folder.getSubChangesFolders().values()){
            getFilesState(subFolder, settings);
        }
    }

    public String getFileContent(String sha1,  Repository repository){
        return String.join("\n",
                FSUtils.getZippedContent(repository.getSettings().getRepositoryObjectsFullPath().getAbsolutePath(),
                sha1));
    }

    public void accept(Repository repository){

        Branch branchToUpdateMerge = null;
        if(repository.getActiveBranch().getName().equals(toBranch))
            branchToUpdateMerge = repository.getActiveBranch();

        Merge merge = repository.getMerge(fromBranch, toBranch, branchToUpdateMerge);

        if(merge.isFastForwardSha1())
            repository.makeFFMergeWebMode(toBranch);
        else{
            repository.makeMerge(merge);
        }
    }


    public Date getCreationTime() {
        return creationTime;
    }

    @Override
    public int compareTo(PullRequest other) {
        return creationTime.compareTo(other.getCreationTime());
    }

}
