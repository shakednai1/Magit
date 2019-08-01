import javax.rmi.CORBA.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

class Branch {

    private Commit startCommit; //TODO add load commit - do not change it after define it
    private Commit head;
    private String name;
    private CommitManager commitManager;

    Branch(String name, Commit head){

        commitManager = new CommitManager();
        if(head == null){
            head = commitManager.commit("", true);
        }
        commitManager.setCurrentCommit(head);

        this.name = name;
        this.head = head;
        startCommit = head;

        writeBranchInfoFile();
    }

    Commit getHead(){ return head; }

    String getName(){
        return name;
    }

    CommitManager getCommitManager(){ return commitManager; }

    boolean haveChanges(){ return commitManager.haveChanges(); }

    boolean commit(String msg, boolean force){
        Commit newCommit = commitManager.commit(msg, force);

        if (newCommit == null)
            return false;

        setHead(newCommit);
        return true;
    }

    void setHead(Commit newHead) {
        head = newHead;
        writeBranchInfoFile();
    }

    void writeBranchInfoFile(){
        String branchFileContent =  head.getSHA1()+ Settings.delimiter + startCommit.getSHA1();
        Utils.writeFile(getBranchFilePath(), branchFileContent, false);
    }

    private String getBranchFilePath(){
        return Settings.branchFolderPath + getName() + ".txt";
    }


    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != startCommit){
            res.add(currentCommit.toString());
            currentCommit = currentCommit.getPreviousCommit();
        }
        return res;
    }

    List<String> getCommittedState(){
        return commitManager.getCommittedItemsData();
    }


    public void open() {
        clearCurrentWC();
        recursiveOpenAllFiles(getRootSha1(), Settings.repositoryFullPath);
    }

    private void clearCurrentWC() {
        File directory = new File(Settings.repositoryFullPath);
        File[] listOfItems = directory.listFiles();
        for(File item: listOfItems){
            if(item.isDirectory()){
                if(!item.getName().equals(Settings.gitFolder)){
                    deleteSubFilesRec(item);
                }
            }
            else{
                Utils.deleteFile(item.getPath());
            }
        }
    }

    private void deleteSubFilesRec(File folder) {
        File directory = new File(folder.getPath());
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory()) {
                deleteSubFilesRec(item);
            }
            else{
                Utils.deleteFile(item.getPath());
            }
        }
        Utils.deleteFile(folder.getPath());
    }


    private String getRootSha1(){
        //String newCommitDir = Settings.objectsFolderPath + head.commitSha1 + "tmp" + ".txt";
        Utils.unzip(Settings.objectsFolderPath + head.getSHA1() + ".zip", Settings.objectsFolderPath);
        try {
            String contents = new String(Files.readAllBytes(Paths.get(
                    Settings.objectsFolderPath + head.getSHA1() + ".txt")));
            Utils.deleteFile(Settings.objectsFolderPath + head.getSHA1() + ".txt");
            return contents.split(Settings.delimiter)[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void recursiveOpenAllFiles(String sha1, String location){
        //String newFile = Settings.objectsFolderPath + sha1 + "tmp" + ".txt";
        Utils.unzip(Settings.objectsFolderPath + sha1 + ".zip", Settings.objectsFolderPath);
        try {
            String contents = new String(Files.readAllBytes(Paths.get(Settings.objectsFolderPath + sha1 + ".txt")));
            Utils.deleteFile(Settings.objectsFolderPath + sha1 + ".txt");
            String[] folderItems = contents.split("\\r\\n");
            for(String item: folderItems){
                String[] itemData = item.split(",");
                String typeOfItem = itemData[2];
                String itemSha1 = itemData[1];
                String itemName = itemData[0];
                String path = location + "/" + itemName;
                if(typeOfItem.equals("File")){
                    openFile(itemSha1, location);
                }
                else{
                    Utils.createFolder(path);
                    recursiveOpenAllFiles(itemSha1, path);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void openFile(String itemSha1, String filePath){
        Utils.unzip(Settings.objectsFolderPath + itemSha1 + ".zip", filePath);
    }

}
