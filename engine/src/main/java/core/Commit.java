package core;

import org.apache.commons.codec.digest.DigestUtils;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit implements CommitRepresentative {

    static DateFormat commitDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");

    private String msg;
    private String commitTime;
    private String commitSha1;
    private String rootSha1;
    private String firstPreviousCommitSHA1;
    private String secondPreviousCommitSHA1; // The commit that was merged with prevCommit of the head branch
    private String userLastModified;

    Commit(String msg, String rootFolderSha, String userLastModified, String commitTime, String firstPreviousCommitSHA1){
        this.msg = msg;
        this.firstPreviousCommitSHA1 = firstPreviousCommitSHA1 == null ? "" : firstPreviousCommitSHA1;
        this.rootSha1 = rootFolderSha;
        this.commitTime = commitTime;
        this.userLastModified = userLastModified;
        commitSha1 = calcCommitSha1();

        zipCommit();
    }

    Commit(String commitSha1){
        this.commitSha1 = commitSha1;

        List<String> content = Utils.getZippedContent(commitSha1);
        String[] commitData = content.get(0).split(Settings.delimiter);

        this.rootSha1 = commitData[0];
        this.commitTime = commitData[commitData.length - 2];
        this.userLastModified = commitData[commitData.length - 1];
        this.firstPreviousCommitSHA1 = commitData[1].equals("null") ? "" : commitData[1];

        String[] msgParts = Arrays.copyOfRange(commitData, 2, commitData.length - 2);
        this.msg = String.join( Settings.delimiter, msgParts);
    }

    @Override
    public String getSha1(){return commitSha1;}
    public String getRootFolderSHA1(){ return rootSha1; }
    public String getFirstPreviousCommitSHA1(){ return firstPreviousCommitSHA1; }
    public String getUserLastModified() { return userLastModified; }
    public String getCommitTime() { return commitTime; }
    public String getMsg(){ return msg;}


    public String toString(){
        return commitSha1 + Settings.delimiter +
                msg + Settings.delimiter +
                commitTime + Settings.delimiter +
                userLastModified ;
    }

    private String calcCommitSha1(){
        return DigestUtils.sha1Hex(getCommitTxt());
    }

    private String getCommitTxt(){
        String prevCommitStr = (firstPreviousCommitSHA1 == null || firstPreviousCommitSHA1.equals(""))? "null": firstPreviousCommitSHA1;

        return rootSha1 + Settings.delimiter +
                prevCommitStr + Settings.delimiter +
                msg + Settings.delimiter +
                commitTime + Settings.delimiter +
                userLastModified + Settings.delimiter;
    }

    void zipCommit(){
        String fileNameWOExtension = Settings.objectsFolderPath + commitSha1;
        Utils.createNewFile(fileNameWOExtension+".txt", getCommitTxt());
        Utils.zip(fileNameWOExtension + ".zip",fileNameWOExtension + ".txt");
        Utils.deleteFile(fileNameWOExtension + ".txt");
    }

    static Map<String, Commit> loadAll(String endCommitSha1){
        Map<String, Commit> commitMap = new HashMap<>();

        String currCommitSha1 = endCommitSha1;
        commitMap.put(currCommitSha1, new Commit(currCommitSha1));

        while (true){
            if(!commitMap.get(currCommitSha1).hasPrecedingCommit()){
                break;
            }
            currCommitSha1 = commitMap.get(currCommitSha1).getFirstPreviousCommitSHA1();
            commitMap.put(currCommitSha1, new Commit(currCommitSha1));
        }

        return commitMap;
    }

    private boolean hasPrecedingCommit(){
        return !firstPreviousCommitSHA1.equals("");
    }

    @Override
    public String getFirstPrecedingSha1() {
        return firstPreviousCommitSHA1;
    }

    @Override
    public String getSecondPrecedingSha1() {
        return "";
    }

    static protected Map<String, Blob> getAllFilesOfCommit(String commitSha1){
        Folder commitFolder = Commit.getCommitRootFolder(commitSha1);
        return commitFolder.getCommittedFilesState(false);
    }

    static Folder getCommitRootFolder(String commitSha1){
        Commit commit = new Commit(commitSha1);

        return new Folder(new File(Settings.repositoryFullPath),
                new ItemSha1(commit.getRootFolderSHA1(), false),
                commit.getUserLastModified(),
                commit.getCommitTime(),false);

    }

}
