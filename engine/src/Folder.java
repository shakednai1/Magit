import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.util.*;


public class Folder extends Item {

    private Set<Item> subItems = new LinkedHashSet<>();
    private Set<Item> tmpItems = new LinkedHashSet<>();
    private String folderDataString = "";
    private String strForSha1 = "";

    public Folder(String path, String name) {
        typeItem = "Folder";
        fullPath = path;
        this.name = name;
    }

    @Override
    public String calculateSha1() {
        folderDataString = "";
        strForSha1 = "";
        tmpItems.clear();
        getAllItems();
        subItems.clear();
        subItems.addAll(tmpItems);
        return sha1;
    }

    public void zipAll(){
        recursiveUpdateUserDate();
        updateUserAndDate();

        folderDataString = "";
        clacFolderDataString();
        recursiveUpdateFolderData();

        recursiveZip();
        zipAndCopy();
    }

    public void recursiveUpdateFolderData(){
        for(Item item : subItems){
            if (item instanceof Folder){
                ((Folder) item).folderDataString = "";
                ((Folder) item).clacFolderDataString();
                ((Folder) item).recursiveUpdateFolderData();
            }
        }
    }

    public void recursiveUpdateUserDate(){
        for(Item item : subItems){
            if (item instanceof Folder){
                for (String key: RepositoryManager.getActiveRepository().getCommitManager().updatedFiles){
                    if (key.startsWith(item.fullPath)){
                        item.updateUserAndDate();
                    }
                }

                for (String key: RepositoryManager.getActiveRepository().getCommitManager().newFiles){
                    if (key.startsWith(item.fullPath)){
                        item.updateUserAndDate();
                    }
                }
                ((Folder) item).recursiveUpdateUserDate();
            }
            else if (item instanceof Blob){
                if (RepositoryManager.getActiveRepository().getCommitManager().newFiles.contains(item.fullPath) ||
                        RepositoryManager.getActiveRepository().getCommitManager().updatedFiles.contains(item.fullPath)){
                    item.updateUserAndDate();
                }
            }
        }
    }

    public void recursiveZip(){
        for(Item item : subItems){
            if (item instanceof Folder){
                ((Folder) item).recursiveZip();
                item.zipAndCopy();
            }
            else if (item instanceof Blob){
                item.zipAndCopy();
            }
        }
    }

    // recursive calculate sha1 for all the sub folders and files under the current directory
    // TODO : verify empty folder sha1 is not calculated
    private void getAllItems() {
        File directory = new File(fullPath);
        File[] listOfItems = directory.listFiles();
        for (File item : listOfItems) {
            if (item.isDirectory() && !item.getName().equals(".magit")) {
                Item folder = getItemFromSubItems(item.getPath());
                if (folder == null){
                    folder = new Folder(item.getPath(), item.getName());
                    ((Folder) folder).getAllItems();
                    subItems.add(folder);
                    tmpItems.add(folder);
                }
                else{
                    tmpItems.add(folder);
                    ((Folder) folder).getAllItems();
                }
            }
            else if(item.isFile()){
                Item file = getItemFromSubItems(item.getPath());
                if (file == null){
                    file = new Blob(item.getPath(), item.getName());
                    file.calculateSha1();
                    subItems.add(file);
                    tmpItems.add(file);
                }
                else{
                    tmpItems.add(file);
                    file.calculateSha1();
                }
                RepositoryManager.getActiveRepository().getCommitManager().newState.put(file.fullPath, file.sha1);
            }
        }
        strForSha1 = "";
        getStringToCalcSha1();
        sha1 = DigestUtils.sha1Hex(strForSha1);
    }

    public Item getItemFromSubItems(String path){
        for(Item item : subItems){
            if (item.fullPath.equals(path)){
                return item;
            }
        }
        return null;
    }

    // create string from the folder data to calculate sha1 + write to zip file under .objects
    private void clacFolderDataString(){
        for (Item item : subItems){
            folderDataString = folderDataString + item.name + Settings.delimiter + item.sha1 +
                    Settings.delimiter + item.typeItem + Settings.delimiter + item.userLastModified +
                    Settings.delimiter + item.lastModified + "\r\n";
        }
    }

    public void getStringToCalcSha1(){
        for (Item item : subItems){
            strForSha1 = strForSha1 + item.name + Settings.delimiter +  item.sha1 + Settings.delimiter
                    + item.typeItem + "\r\n";
        }
    }

    // create txt file with folder data - and zip the file. delete the txt file
    @Override
    public void zipAndCopy(){
        if (!isExistInObjects()){
            Utils.createNewFile(getTxtFilePath(), folderDataString);
            Utils.zip(getZipPath(), getTxtFilePath());
            Utils.deleteFile(getTxtFilePath());
        }
    }


    // returns the txt file path (we create this file -> zip it -> delete)
    private String getTxtFilePath(){
        return RepositoryManager.getActiveRepository().getObjectsFolderPath() + sha1 + ".txt";
    }

    public void addAllItemsToCurentCommit(){
        RepositoryManager.getActiveRepository().getCommitManager().currentCommit.allItems.add(fullPath + Settings.delimiter + typeItem +
                Settings.delimiter + sha1 + Settings.delimiter + userLastModified +
                Settings.delimiter + lastModified);
        getAllItemsToCommit();
    }

    private void getAllItemsToCommit(){
        for(Item item : subItems){
            if (item instanceof Folder){
                ((Folder) item).getAllItemsToCommit();
                addToCommitAllItems(item);
            }
            else if (item instanceof Blob){
                addToCommitAllItems(item);
            }
        }
    }

    private void addToCommitAllItems(Item item){
        RepositoryManager.getActiveRepository().getCommitManager().currentCommit.allItems.add(item.fullPath + Settings.delimiter + item.typeItem +
                Settings.delimiter + item.sha1 + Settings.delimiter + item.userLastModified +
                Settings.delimiter + item.lastModified);
    }


}