package pullRequest;

import core.FSUtils;
import core.ItemSha1;
import core.RemoteBranch;
import core.Settings;
import models.BranchData;
import org.apache.commons.io.FileUtils;
import user.notification.NewPRNotification;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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


    public PullRequest(String ownerUser, String remoteRepoName, File prFolder){

        this.ownerUser = ownerUser;
        this.remoteRepoName = remoteRepoName;
        setSha1(prFolder);

        setDetailsFromFile();
        setStateFromFile();
    }


    private void setSha1(File prFolder){
        sha1 = new ItemSha1(prFolder.getName(), false, false,getPullRequestFolder());
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

        for(File prSha1: pullRequestsFolder.listFiles()){
            res.add(new PullRequest(ownerUser, repoName, prSha1));
        }
        return res;
    }

}
