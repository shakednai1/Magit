package core;

import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Commit {

    static DateFormat commitDateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");

    private String msg;
    private String commitTime;
    private String commitSha1;
    private String rootSha1;
    private String previousCommitSHA1;
    private String userLastModified;
    private String mergedCommitSHA1; // The commit that was merged with prevCommit of the head branch

    Commit(String msg, String rootFolderSha, String userLastModified, String commitTime, String previousCommitSHA1){
        this.msg = msg;
        this.previousCommitSHA1 = previousCommitSHA1;
        this.rootSha1 = rootFolderSha;
        this.commitTime = commitTime;
        this.userLastModified = userLastModified;
        commitSha1 = calcCommitSha1();

        zipCommit();
    }

    Commit(String commitSha1){
        this.commitSha1 = commitSha1;
        Utils.unzip(Settings.objectsFolderPath + commitSha1 + ".zip", Settings.objectsFolderPath, commitSha1 + ".txt");
        List<String> content = Utils.getFileLines(Settings.objectsFolderPath + commitSha1 + ".txt");
        Utils.deleteFile(Settings.objectsFolderPath + commitSha1 + ".txt");

        String[] commitData = content.get(0).split(Settings.delimiter);

        this.rootSha1 = commitData[0];
        this.commitTime = commitData[commitData.length - 2];
        this.userLastModified = commitData[commitData.length - 1];
        this.previousCommitSHA1 = commitData[1];

        String[] msgParts = Arrays.copyOfRange(commitData, 2, commitData.length - 2);
        this.msg = String.join( Settings.delimiter, msgParts);
    }

    public String getCommitSHA1(){return commitSha1;}
    public String getRootFolderSHA1(){ return rootSha1; }
    public String getPreviousCommitSHA1(){ return previousCommitSHA1; }
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
        String prevCommitStr = (previousCommitSHA1 == null)? "null": previousCommitSHA1;

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
            currCommitSha1 = commitMap.get(currCommitSha1).getPreviousCommitSHA1();
            if(currCommitSha1.equals("null")){
                break;
            }
            commitMap.put(currCommitSha1, new Commit(currCommitSha1));
        }

        return commitMap;
    }


}
