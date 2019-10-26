package pullRequest;

import core.*;
import exceptions.InvalidBranchNameError;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.BranchData;
import org.apache.commons.io.FileUtils;
import user.notification.NewPRNotification;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

import static user.notification.Notification.NotificationType.NEW_PULL_REQUEST;

public class PullRequest {

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

    public String getComment() {
        return comment;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public String getToBranch() {
        return toBranch;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public PRStatus getStatus() {
        return status;
    }

    public void setStatus(PRStatus status) {
        this.status = status;
    }

    public ItemSha1 getSha1() {
        return sha1;
    }


    String repoName;
    String remoteRepoName;
    String requestingUser;
    String ownerUser;
    String comment;
    String  fromBranch;
    String toBranch;
    Date creationTime;
    PRStatus status;
    ItemSha1 sha1 ;


    List<String> newFiles = new ArrayList<>();
    List<String> updateFiles = new ArrayList<>();
    List<String> deleteFiles = new ArrayList<>();


    static enum PRStatus {NEW, ACCEPTED, DECLINED};

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
        this.toBranch = toBranch.getName();
        this.creationTime = new Date();
        status = PRStatus.NEW;

        sha1 = new ItemSha1(toString(), true, false, getPullRequestFolder());
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
        toBranch = fields[4];
        comment = fields[5];
    }

    private void setStateFromFile(){
        List<String> content = FSUtils.getFileLines(getPRStateFile().getAbsolutePath());
        status = PRStatus.valueOf(content.get(0));
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
                toBranch,
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
        BranchData fromBranch = repository.getBranchByName(this.fromBranch);
        BranchData toBranch = repository.getBranchByName(this.toBranch);

        setFilesDeltaCommit(fromBranch.getHeadSha1(), toBranch.getHeadSha1());
    }

    public void setFilesDeltaCommit(String commitSha1, String prevCommit){
        FolderChanges folderChanges = CommitsDelta.getDiffBetweenCommits(commitSha1, prevCommit, null);

        for(FileChanges file : folderChanges.getSubChangesFiles().values()){
            Common.FilesStatus status = file.getState();
            String path = file.getFullPath();
            if(status == Common.FilesStatus.NEW) {
                newFiles.add(path);
            }
            else if(status == Common.FilesStatus.UPDATED) {
                updateFiles.add(path);
            }
            else if(status == Common.FilesStatus.DELETED) {
                deleteFiles.add(path);
            }
        }
    }


}
