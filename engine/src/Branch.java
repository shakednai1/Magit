import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Branch {

    private Commit startCommit;
    private Commit head;
    private String name;
    private Folder rootFolder;

    private Map<String, String> currentStateOfFiles = new HashMap<>();
    private Map<String, String> newStateOfFiles = new HashMap<>();

    Branch(String name, Commit head){
        this.name = name;

        if(head == null){
            rootFolder = getRootFolder();
            rootFolder.updateState();
            commit("", true);

        }
        else{
            this.head = head;
            startCommit = head;
            rootFolder = head.getRootFolder();
            writeBranchInfoFile();

            currentStateOfFiles = rootFolder.getCommittedItemsState();
        }
    }

    private Folder getRootFolder(){
        String repoLocation = Settings.repositoryFullPath;
        Path path = Paths.get(repoLocation);
        String repoName = path.getFileName().toString();
        return new Folder(repoLocation, repoName);
    }

    Commit getHead(){ return head; }

    String getName(){
        return name;
    }

    boolean haveChanges(){ return head.haveChanges(); }

    boolean commit(String msg, boolean force){
        if(head == null || haveChanges() || force){
            Commit com = new Commit(msg, rootFolder, head);
            com.zipCommit();

            setHead(com);

            currentStateOfFiles = rootFolder.getCommittedItemsState();
            return true;
        }

        return false;
    }

    private void updateChangedFilesState(){ // TODO better name for function
        newStateOfFiles.clear();
        newStateOfFiles = rootFolder.getCurrentItemsState();
    }

    private void setHead(Commit newHead) {
        head = newHead;
        if (startCommit == null) startCommit = head;

        writeBranchInfoFile();
    }

    private void writeBranchInfoFile(){
        String branchFileContent =  head.getSHA1()+ Settings.delimiter + startCommit.getSHA1();
        Utils.writeFile(getBranchFilePath(name), branchFileContent, false);
    }

    private static String getBranchFilePath(String branchName){
        return Settings.branchFolderPath + branchName + ".txt";
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
        return head.getCommittedItemsData();
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


    Map<String ,List<String>> getWorkingCopy(){
        rootFolder.updateState();
        updateChangedFilesState();
        return getFilesChanges();
    }

    private Map<String, List<String>> getFilesChanges() {
        List<String> newFiles = new ArrayList<>(CollectionUtils.subtract(newStateOfFiles.keySet(), currentStateOfFiles.keySet()));
        List<String> deletedFiles = new ArrayList<>(CollectionUtils.subtract(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        List<String> updatedFiles = new ArrayList<>();

        //check for updated files
        List<String> common = new ArrayList<>(CollectionUtils.retainAll(currentStateOfFiles.keySet(), newStateOfFiles.keySet()));
        for(String key : common){
            if (!currentStateOfFiles.get(key).equals(newStateOfFiles.get(key))){
                updatedFiles.add(key);
            }
        }
        Map<String, List<String>> changes = new HashMap<>();
        changes.put("update", updatedFiles);
        changes.put("new", newFiles);
        changes.put("delete", deletedFiles);
        return changes;
    }

    static boolean deleteBranch(String branchName){
        return Utils.deleteFile(getBranchFilePath(branchName));
    }

}
