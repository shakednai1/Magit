import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Commit {
//TODO change class params to private and add appropriate methods

    String msg;
    String commitTime;
    String commitSha1;
    String rootSha1;
    Folder rootFolder;
    Commit previousCommit;

    private Boolean isMasterCommit;

    Commit(String msg, String rootSha1, Folder rootFolder, Commit previousCommit){
        this.msg = msg;
        this.rootSha1 = rootSha1;
        this.rootFolder = rootFolder;
        this.previousCommit = previousCommit;
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");
        Date date = new Date();
        commitTime = dateFormat.format(date);
        commitSha1 = calcCommitSha1();
        isMasterCommit = this.previousCommit == null;
    }

    private String calcCommitSha1(){
        return DigestUtils.sha1Hex(getCommitTxt());
    }

    private String getCommitTxt(){
        String commitStr =  rootSha1 + Settings.delimiter + msg + Settings.delimiter + commitTime +
                Settings.delimiter + MainEngine.currentUser + Settings.delimiter;
        if(previousCommit == null){
            return commitStr + "null";
        }
        return commitStr + previousCommit.commitSha1;
    }

    void zipCommit(){
        String fileNameWOExtension = Settings.objectsFolderPath + commitSha1;
        Utils.createNewFile(fileNameWOExtension+".txt", getCommitTxt());
        Utils.zip(fileNameWOExtension + ".zip",fileNameWOExtension + ".txt");
        Utils.deleteFile(fileNameWOExtension + ".txt");
    }

}
