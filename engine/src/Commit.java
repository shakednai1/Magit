import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class Commit {

    private String msg;
    private String commitTime;
    private String commitSha1;
    private String rootSha1;
    private Folder rootFolder;
    private Commit previousCommit;

    Commit(String msg, String rootSha1, Folder rootFolder, Commit previousCommit){
        this.msg = msg;
        this.rootSha1 = rootSha1;
        this.rootFolder = rootFolder;
        this.previousCommit = previousCommit;
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");
        Date date = new Date();
        commitTime = dateFormat.format(date);
        commitSha1 = calcCommitSha1();
    }

    String getSHA1(){return rootSha1;}

    String getCommitTime(){ return commitTime; }

    Commit getPreviousCommit(){ return previousCommit; }

    public String toString(){
        return commitSha1 + Settings.delimiter +
                msg + Settings.delimiter +
                commitTime + Settings.delimiter +
                rootFolder.userLastModified ;
    }

    private String calcCommitSha1(){
        return DigestUtils.sha1Hex(getCommitTxt());
    }

    private String getCommitTxt(){
        String commitStr =  rootSha1 + Settings.delimiter +
                msg + Settings.delimiter +
                commitTime + Settings.delimiter +
                Settings.getUser()+ Settings.delimiter;

        commitStr = commitStr + ((previousCommit == null)? "null": previousCommit.commitSha1);

        return commitStr;
    }

    void zipCommit(){
        String fileNameWOExtension = Settings.objectsFolderPath + commitSha1;
        Utils.createNewFile(fileNameWOExtension+".txt", getCommitTxt());
        Utils.zip(fileNameWOExtension + ".zip",fileNameWOExtension + ".txt");
        Utils.deleteFile(fileNameWOExtension + ".txt");
    }

}
