import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit {
//TODO change class params to private and add appropriate methods

    public String msg;
    public String commitTime;
    public String commitSha1;
    public String rootSha1;
    public Folder rootFolder;
    public List<String> allItems = new ArrayList<>();
    public Commit previousCommit;

    public Commit(String msg, String rootSha1, Folder rootFolder, Commit previousCommit){
        this.msg = msg;
        this.rootSha1 = rootSha1;
        this.rootFolder = rootFolder;
        this.previousCommit = previousCommit;
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss:SSS");
        Date date = new Date();
        commitTime = dateFormat.format(date);
        commitSha1 = calcCommitSha1();
    }

    public List<String> getAllItems(){
        return allItems;
    }

    private String calcCommitSha1(){
        return DigestUtils.sha1Hex(getCommitTxt());
    }

    private String getCommitTxt(){
        return rootSha1 + Settings.delimiter + msg + Settings.delimiter + commitTime +
                Settings.delimiter + MainEngine.currentUser;
    }

    public void createCommit(){
        String fileNameWOExtension = RepositoryManager.getActiveRepository().getObjectsFolderPath() + commitSha1;
        Utils.createNewFile(fileNameWOExtension+".txt", getCommitTxt());
        Utils.zip(fileNameWOExtension + ".zip",fileNameWOExtension + ".txt");
        Utils.deleteFile(fileNameWOExtension + ".txt");
    }

}
