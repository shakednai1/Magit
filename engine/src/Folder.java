import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class Folder extends Item {

    final private String typeItem = "Folder";

    private Map<String, Blob> subFiles = new HashMap<>();
    private Map<String, Folder> subFolders = new HashMap<>();

    public Map<String, Blob> curSubFiles = new HashMap<>();
    private Map<String, Folder> curSubFolders = new HashMap<>();

    Folder(String path, String name) {
        fullPath = path;
        this.name = name;
    }

    Folder(File folderPath, String folderSha1, String lastUser, String lastModified , boolean rewriteFS){
         this.name = folderPath.getName();
         this.fullPath = folderPath.getPath();
         this.userLastModified = lastUser;
         this.lastModified = lastModified;
         this.currentSHA1 = folderSha1;

        Utils.unzip(Settings.objectsFolderPath + folderSha1 + ".zip", Settings.objectsFolderPath , folderSha1 + ".txt");
        List<String > content = Utils.getFileLines(Settings.objectsFolderPath + folderSha1 + ".txt");
        Utils.deleteFile(Settings.objectsFolderPath + folderSha1 + ".txt");

        for (String itemData: content){
            String[] item = itemData.split(Settings.delimiter);

            String itemName = item[0];
            String itemFullPath = this.fullPath + "/" + itemName;
            String itemSha1 = item[1];
            String itemType = item[2];
            String itemLastUser = item[3];
            String itemLastModified = item[4];

            if (itemType.equals("File")){
                subFiles.put(itemFullPath, new Blob(new File(itemFullPath), itemSha1, itemLastUser, itemLastModified, rewriteFS));
            }
            else{
                subFolders.put(itemFullPath, new Folder(new File(this.fullPath +"/"+ itemName),
                        itemSha1, itemLastUser, itemLastModified, rewriteFS));
            }
        }
    }

    Folder(String path, String name,  String lastUser, String lastModified) {
        fullPath = path;
        this.name = name;
        this.lastModified = lastModified;
        this.userLastModified = lastUser;
    }


    public void setSubItems(Map<String, Blob> subFiles, Map<String, Folder> subFolders){
        this.subFiles = subFiles;
        this.curSubFiles = subFiles;
        this.subFolders = subFolders;
        this.curSubFolders = subFolders;

    }

    boolean commit(String commitUser, String commitTime){
        // function assume the items are up-to-date
        boolean subItemsChanged = false;

        subFolders = curSubFolders;
        for(Folder folder: subFolders.values()){
            boolean subFolderChanged = folder.commit(commitUser, commitTime);

            subItemsChanged = subItemsChanged || subFolderChanged;
        }

        for(Blob file: curSubFiles.values()){
            Blob prevFile = subFiles.get(file.fullPath);
            if(prevFile == null || (!prevFile.currentSHA1.equals(file.currentSHA1))){
                file.updateUserAndDate(commitUser, commitTime);
                subItemsChanged = true;
            }
            file.zipAndCopy();
        }
        subFiles = curSubFiles;

        if(subItemsChanged){
            updateUserAndDate(commitUser, commitTime);
        }

        curSubFiles = new HashMap<>();
        curSubFolders = new HashMap<>();

        zipAndCopy();

        return subItemsChanged;
    }

    public void setSHA1(){
        String sha1Str = getStringToCalcSHA1();
        currentSHA1 = DigestUtils.sha1Hex(sha1Str) ;
    }

    public void zipRec(){
        for (Blob blob: subFiles.values()){
            blob.zipAndCopy();
        }

        for(Folder folder: subFolders.values()){
            folder.zipRec();
        }
        zipAndCopy();
    }

    @Override
    public void updateState(){
        curSubFiles = new HashMap<>();
        curSubFolders = new HashMap<>();

        File directory = new File(fullPath);
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory() && !item.getName().equals(Settings.gitFolder)) {
                Folder folder = subFolders.get(item.getPath());

                if (folder == null) {
                    folder = new Folder(item.getPath(), item.getName());
                }
                folder.updateState();
                if(folder.currentSHA1 != null)
                    curSubFolders.put(folder.fullPath, folder);
            }
            else if(item.isFile()){
                Blob file = subFiles.get(item.getPath());

                if (file == null) {
                    file = new Blob(item.getPath(), item.getName());
                }

                file.updateState();
                curSubFiles.put(file.fullPath, file);
            }
        }

        setSHA1();
    }

    // create string from the folder data to calculate currentSHA1 + write to zip file under .objects
    private String getFolderDataString(){
        String folderDataString = "";
        for(Item item: getOrderedItems(subFiles, subFolders)){
            folderDataString = folderDataString +
                    item.name + Settings.delimiter +
                    item.currentSHA1 + Settings.delimiter +
                    item.getTypeItem() + Settings.delimiter +
                    item.userLastModified + Settings.delimiter +
                    item.lastModified + "\r\n";
        }
        return folderDataString;
    }

    private String getStringToCalcSHA1(){
        String strForSha1 = "";
        for(Item item: getOrderedItems(curSubFiles, curSubFolders)){
            strForSha1 = strForSha1 +
                    item.name + Settings.delimiter +
                    item.currentSHA1 + Settings.delimiter +
                    item.getTypeItem() + "\r\n";
        }

        return strForSha1;
    }

    private List<Item> getOrderedItems(Map<String , Blob> fileItems, Map<String , Folder> folderItems){
        List<Item> ordItems = new LinkedList<>();

        ordItems.addAll(fileItems.values());
        ordItems.addAll(folderItems.values());

        ordItems.sort(compareToOther);
        return ordItems;
    }

    // create txt file with folder data - and zip the file. delete the txt file
    @Override
    public void zipAndCopy(){
        if (!isExistInObjects()){
            Utils.createNewFile(getTxtFilePath(), getFolderDataString());
            Utils.zip(getZipPath(), getTxtFilePath());
            Utils.deleteFile(getTxtFilePath());
        }
    }

    private String getTxtFilePath(){
        // returns the txt file path (we create this file -> zip it -> delete)
        return Settings.objectsFolderPath + currentSHA1 + ".txt";
    }

    List<String> getItemsData() {
        List<String> itemsData = new ArrayList<>();

        for (Blob file : subFiles.values()) {
            itemsData.add(file.getDataString());
        }

        for (Folder folder : subFolders.values()) {
            itemsData.add(folder.getDataString());
            itemsData.addAll(folder.getItemsData());
        }

        return itemsData;
    }

    Map<String, String > getCommittedItemsState(){
        return getItemsState(true);
    }

    Map<String, String > getCurrentItemsState(){
        return getItemsState(false);
    }

    private Map<String, String > getItemsState(boolean committed){
        Map<String, String > itemsState = new HashMap<>();

        Map<String, Blob> files = (committed)? subFiles : curSubFiles;
        Map<String, Folder> folders = (committed)? subFolders: curSubFolders;

        for (Blob file : files.values()){
            itemsState.put(file.fullPath, file.currentSHA1);
        }

        for (Folder folder: folders.values()){
            itemsState.putAll(folder.getItemsState(committed));
        }

        return itemsState;
    }

    String getTypeItem(){ return this.typeItem; }


}