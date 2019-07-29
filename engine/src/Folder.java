import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.*;

public class Folder extends Item {

    private Map<String, Blob> subFiles = new HashMap<>();
    private Map<String, Folder> subFolders = new HashMap<>();

    private Map<String, Blob> curSubFiles = new HashMap<>();
    private Map<String, Folder> curSubFolders = new HashMap<>();

    Folder(String path, String name) {
        typeItem = "Folder";
        fullPath = path;
        this.name = name;
    }

    @Override
    public String updateStateAndSetSha1() {
        updateCurrentState();
        setSHA1();
        return currentSHA1;
    }

    private void setSHA1(){
        String sha1Str = getStringToCalcSHA1();
        currentSHA1 = DigestUtils.sha1Hex(sha1Str);
    }

    boolean commit(String commitUser, String commitTime){
        // function assume the items are up-to-date

        boolean filesHadBeenUpdated;
        boolean subFolderUpdatedFiles = false;

        filesHadBeenUpdated = isFoldersChanged() | isFilesChanged();

        subFolders = curSubFolders;
        subFiles = curSubFiles;

        for(Folder folder: subFolders.values()){
            subFolderUpdatedFiles |= folder.commit(commitUser, commitTime);
        }

        if (subFolderUpdatedFiles | filesHadBeenUpdated) {
            updateUserAndDate(commitUser, commitTime);
            filesHadBeenUpdated = true;
        }

        zipAndCopy();

        return filesHadBeenUpdated;
    }

    private Boolean isFilesChanged(){    // TODO -  merge with    isFoldersChanged
        for (Item item: subFiles.values()){
            String newSHA1 = curSubFiles.get(item.fullPath).currentSHA1;

            if(!newSHA1.equals(item.currentSHA1))
                return true;
        }
        return false;
    }

    private Boolean isFoldersChanged(){
        for (Item item: subFolders.values()){
          String newSHA1 = curSubFolders.get(item.fullPath).currentSHA1;

          if(!newSHA1.equals(item.currentSHA1))
              return true;
        }
        return false;

    }

    private void updateCurrentState(){
        curSubFiles.clear();
        curSubFolders.clear();

        File directory = new File(fullPath);
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory() && !item.getName().equals(Settings.gitFolder)) {
                Folder folder = subFolders.get(item.getPath());

                if (folder == null) {
                    folder = new Folder(item.getPath(), item.getName());
                }
                folder.updateCurrentState();
                curSubFolders.put(folder.fullPath, folder);
            }
            else if(item.isFile()){
                Blob file = subFiles.get(item.getPath());

                if (file == null) {
                    file = new Blob(item.getPath(), item.getName());
                }

                file.updateStateAndSetSha1();
                curSubFiles.put(file.fullPath, file);
            }
        }
    }

    // create string from the folder data to calculate currentSHA1 + write to zip file under .objects
    private String getFolderDataString(){
        String folderDataString = "";
        for(Item item: getOrderedItems(subFiles, subFolders)){
            folderDataString = folderDataString + item.name + Settings.delimiter + item.currentSHA1 +
                    Settings.delimiter + item.typeItem + Settings.delimiter + item.userLastModified +
                    Settings.delimiter + item.lastModified + "\r\n";
        }
        return folderDataString;
    }

    private String getStringToCalcSHA1(){
        String strForSha1 = "";
        for(Item item: getOrderedItems(curSubFiles, curSubFolders)){
            strForSha1 = strForSha1 + item.name + Settings.delimiter +  item.currentSHA1 + Settings.delimiter
                    + item.typeItem + "\r\n";
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

}