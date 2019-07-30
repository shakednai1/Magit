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


    Branch(String name, Commit head){
        this.name = name;
        this.head = head;
        startCommit = head;
    }

    Commit getHead(){
        return head;
    }

    String getName(){
        return name;
    }

    void setHead(Commit newHead) {
        head = newHead;
        Utils.deleteFile(Settings.branchFolderPath + name + ".txt");
        Utils.createNewFile(Settings.branchFolderPath + name + ".txt", newHead.commitSha1 + Settings.delimiter + startCommit.commitSha1);

    }

    List<String> getCommitHistory(){
        List<String> res = new LinkedList<>();
        Commit currentCommit = head;
        while (currentCommit != startCommit){
            res.add(currentCommit.commitSha1 + Settings.delimiter + currentCommit.msg + Settings.delimiter +
                    currentCommit.commitTime + Settings.delimiter + currentCommit.rootFolder.userLastModified);
            currentCommit = currentCommit.previousCommit;
        }
        return res;
    }

    public Commit getStartCommit(){
        return startCommit;
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
        Utils.unzip(Settings.objectsFolderPath + head.commitSha1 + ".zip", Settings.objectsFolderPath);
        try {
            String contents = new String(Files.readAllBytes(Paths.get(
                    Settings.objectsFolderPath + head.commitSha1 + ".txt")));
            Utils.deleteFile(Settings.objectsFolderPath + head.commitSha1 + ".txt");
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
