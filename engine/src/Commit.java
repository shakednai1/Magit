import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class Commit {

    private String msg;
    private String commitTime;
    private String commitSha1;
    private Folder rootFolder;
    private String rootSha1;
    private Commit previousCommit;

    Commit(String msg,Folder rootFolder, Commit previousCommit){
        this.msg = msg;
        this.rootFolder = rootFolder;
        this.previousCommit = previousCommit;
        this.rootSha1 = rootFolder.currentSHA1;

        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");
        commitTime = dateFormat.format(new Date());

        rootFolder.commit(Settings.getUser(), commitTime);
        commitSha1 = calcCommitSha1();
    }

    String getSHA1(){return commitSha1;}

    Commit getPreviousCommit(){ return previousCommit; }

    Folder getRootFolder() { return rootFolder; }

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
        String commitStr =  rootFolder.currentSHA1 + Settings.delimiter +
                msg + Settings.delimiter +
                commitTime + Settings.delimiter +
                Settings.getUser()+ Settings.delimiter;

        commitStr = commitStr + ((previousCommit == null)? "null": previousCommit.getSHA1());

        return commitStr;
    }

    void zipCommit(){
        String fileNameWOExtension = Settings.objectsFolderPath + commitSha1;
        Utils.createNewFile(fileNameWOExtension+".txt", getCommitTxt());
        Utils.zip(fileNameWOExtension + ".zip",fileNameWOExtension + ".txt");
        Utils.deleteFile(fileNameWOExtension + ".txt");
    }

    List<String> getCommittedItemsData(){ return rootFolder.getItemsData(); }

    boolean haveChanges(){
        rootFolder.updateState();
        return !rootSha1.equals(rootFolder.currentSHA1); }


}
