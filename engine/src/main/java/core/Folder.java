package core;

import java.io.File;
import java.util.*;

public class Folder extends Item {

    final private String typeItem = Common.ItemTypes.Folder.name();

    protected Map<String, Blob> subFiles = new HashMap<>();
    protected Map<String, Folder> subFolders = new HashMap<>();

    private Map<String, Blob> curSubFiles = new HashMap<>();
    private Map<String, Folder> curSubFolders = new HashMap<>();


    Folder(){}

    Folder(File folderPath, Settings repoSettings) {
        fullPath = folderPath.getAbsolutePath();
        name = folderPath.getName();
        this.repoSettings = repoSettings;
    }

    Folder(File folderPath, ItemSha1 folderSha1, String lastUser, String lastModified, Settings repoSettings ){
        this.fullPath = folderPath.getAbsolutePath();
        this.name = folderPath.getName();
        this.userLastModified = lastUser;
        this.lastModified = lastModified;
        this.currentSHA1 = folderSha1;
        this.repoSettings = repoSettings;

        for (String itemData: this.currentSHA1.getContent().split("\\n")){
            String[] item = itemData.split(Settings.delimiter);

            String itemName = item[0];
            String itemFullPath = new File(this.fullPath , itemName).getPath();
            String itemSha1 = item[1];
            String itemType = item[2];
            String itemLastUser = item[3];
            String itemLastModified = item[4];

            if (itemType.equals("File")){
                subFiles.put(itemFullPath, new Blob(new File(itemFullPath),
                        new ItemSha1(itemSha1, false, false, repoSettings.getRepositoryObjectsFullPath()),
                        itemLastUser,
                        itemLastModified,
                        repoSettings));
            }
            else{
                subFolders.put(itemFullPath, new Folder(new File(itemFullPath),
                        new ItemSha1(itemSha1, false, false, repoSettings.getRepositoryObjectsFullPath()),
                        itemLastUser,
                        itemLastModified,
                        repoSettings));
            }
        }
    }

    Folder(File fullPath,  String lastUser, String lastModified, Settings repoSettings) {
        this.fullPath = fullPath.getAbsolutePath();
        this.name = fullPath.getName();
        this.lastModified = lastModified;
        this.userLastModified = lastUser;
        this.repoSettings = repoSettings;
    }

    boolean isEmptyCurrentState(){
        return (curSubFiles.isEmpty() && curSubFolders.isEmpty());
    }

    void setSubItems(Map<String, Blob> subFiles, Map<String, Folder> subFolders){
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
            file.zip();
        }
        subFiles = curSubFiles;

        if(subItemsChanged){
            updateUserAndDate(commitUser, commitTime);
        }

        curSubFiles = new HashMap<>();
        curSubFolders = new HashMap<>();

        zip();

        return subItemsChanged;
    }

    void setSHA1(){
        String sha1Str = getStringToCalcSHA1();
        currentSHA1 = new ItemSha1(sha1Str, true, false, repoSettings.getRepositoryObjectsFullPath());
    }

    void zipRec(){
        for(Folder folder: subFolders.values()) folder.zipRec();

        for (Blob blob: subFiles.values()) blob.zip();

        zip();
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
                    folder = new Folder(item, repoSettings);
                }
                folder.updateState();
                if(!folder.isEmptyCurrentState())
                    curSubFolders.put(folder.fullPath, folder);
            }
            else if(item.isFile()){
                Blob file = new Blob(item.getPath(), repoSettings);
                file.updateState();

                Blob prevFile = subFiles.get(file.fullPath);
                if (prevFile != null){
                    file.updateUserAndDate(prevFile.getUser(), prevFile.getModifiedTime());
                }

                curSubFiles.put(file.fullPath, file);
            }
        }

        setSHA1();
    }

    void rewriteFS(){
        FSUtils.clearWC(fullPath);
        _rewriteFS();
    }

    private void _rewriteFS(){
        for(Blob file: subFiles.values()){
            file.rewriteFS();
        }
        for(Folder folder: subFolders.values()){
            folder._rewriteFS();
        }
    }

    // create string from the folder data to calculate currentSHA1 + write to zip file under .objects
    private String getFolderDataString(){
        String folderDataString = "";
        for(Item item: getOrderedItems(subFiles, subFolders)){
            folderDataString = folderDataString +
                    item.name + Settings.delimiter +
                    item.currentSHA1.sha1 + Settings.delimiter +
                    item.getTypeItem() + Settings.delimiter +
                    item.userLastModified + Settings.delimiter +
                    item.lastModified + "\r\n";
        }
        return folderDataString;
    }

    private String getStringToCalcSHA1(){
        String strForSha1 = "";
        for(Item item: getOrderedItemsForSha1()){
            strForSha1 = strForSha1 +
                    item.name + Settings.delimiter +
                    item.currentSHA1.sha1 + Settings.delimiter +
                    item.getTypeItem() + "\r\n";
        }

        return strForSha1;
    }

    protected List<Item> getOrderedItemsForSha1(){
        return getOrderedItems(curSubFiles, curSubFolders);
    }

    protected List<Item> getOrderedItems(Map<String , Blob> fileItems, Map<String , Folder> folderItems){
        List<Item> ordItems = new LinkedList<>();

        ordItems.addAll(fileItems.values());
        ordItems.addAll(folderItems.values());

        ordItems.sort(compareToOther);
        return ordItems;
    }

    // create txt file with folder data - and zip the file. delete the txt file
    @Override
    public void zip(){
        if (!isExistInObjects()){
            FSUtils.createNewFile(getTxtFilePath(), getFolderDataString());
            FSUtils.zip(getZipPath(), getTxtFilePath());
            FSUtils.deleteFile(getTxtFilePath());
        }
    }

    private String getTxtFilePath(){
        // returns the txt file path (we create this file -> zip it -> delete)
        return repoSettings.objectsFolderPath + getSha1() + ".txt";
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

    Map<String, Blob > getCommittedFilesState(boolean direct){
        return getFilesState(true, direct);
    }

    Map<String, Blob > getCurrentFilesState(boolean direct){
        return getFilesState(false, direct);
    }

    private Map<String, Blob > getFilesState(boolean committed, boolean direct){
        Map<String, Blob > itemsState = new HashMap<>();

        Map<String, Blob> files = (committed)? subFiles : curSubFiles;

        for (Blob file : files.values()){ itemsState.put(file.fullPath, file); }

        if(!direct){
            Map<String, Folder> folders = (committed)? subFolders: curSubFolders;

            for (Folder folder: folders.values()){
                itemsState.putAll(folder.getFilesState(committed, false));
            }
        }

        return itemsState;
    }

    public Map<String , Folder> getSubFolders(){ return new HashMap<>(subFolders); }

    public Map<String , Blob> getSubFiles(){ return new HashMap<>(subFiles); }


    String getTypeItem(){ return this.typeItem; }


}