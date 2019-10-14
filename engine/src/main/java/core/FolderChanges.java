package core;

import java.io.File;
import java.text.ParseException;
import java.util.*;

public class FolderChanges extends Folder {

    // TODO maybe need to extend Folder

    Folder baseElement;
    Folder aElement;
    Folder bElement;

    private Map<String, FileChanges> subChangesFiles = new HashMap<>();
    private Map<String, FolderChanges> subChangesFolders = new HashMap<>();

    private boolean hasConflicts;

    private boolean folderDeleted;


    FolderChanges(Folder baseFolder, Folder aFolder, Folder bFolder){
        this.baseElement = baseFolder;
        this.aElement = aFolder;
        this.bElement = bFolder;

        setBasicDetails();

        setSubFilesChanges();
        setSubFoldersChanges();

        setHasConflicts();
        // at this point, no sha1 need to be calc. only at commit
    }

    private void setBasicDetails(){
        Folder dataElement = (this.baseElement != null)? this.baseElement :
                (this.aElement != null)? this.aElement:
                        this.bElement;

        this.fullPath = dataElement.fullPath;
        this.name = dataElement.name;
    }

    private void setSubFilesChanges() {
        Map<String, Blob> baseSubFiles = (baseElement != null)? baseElement .getCommittedFilesState(true): new HashMap<>();
        Map<String, Blob> aSubFiles = (aElement != null)? aElement.getCommittedFilesState(true): new HashMap<>();
        Map<String, Blob> bSubFiles = (bElement != null)? bElement.getCommittedFilesState(true): new HashMap<>();

        for (Map.Entry<String, Blob> file : baseSubFiles.entrySet()) {
            String filePath = file.getValue().fullPath;
            subChangesFiles.put(filePath, new FileChanges(file.getValue(), aSubFiles.get(filePath), bSubFiles.get(filePath)));

            aSubFiles.remove(filePath);
            bSubFiles.remove(filePath);
        }
        baseSubFiles.clear();

        for (Map.Entry<String, Blob> file : aSubFiles.entrySet()) {
            String filePath = file.getValue().fullPath;
            subChangesFiles.put(filePath, new FileChanges(null, file.getValue(), bSubFiles.get(filePath)));

            bSubFiles.remove(filePath);
        }
        aSubFiles.clear();

        for (Map.Entry<String, Blob> file : bSubFiles.entrySet()) {
            String filePath = file.getValue().fullPath;
            subChangesFiles.put(filePath, new FileChanges(null, null, file.getValue()));
        }
        bSubFiles.clear();
    }

    private void setSubFoldersChanges(){
        Map<String, Folder> baseSubFolders = (baseElement != null)? baseElement.getSubFolders(): new HashMap<>();
        Map<String, Folder> aSubFolders = (aElement != null)? aElement.getSubFolders(): new HashMap<>();
        Map<String, Folder> bSubFolders = (bElement != null)? bElement.getSubFolders(): new HashMap<>();

        for (Map.Entry<String, Folder> folder : baseSubFolders.entrySet()) {
            String filePath = folder.getKey();
            subChangesFolders.put(filePath, new FolderChanges(folder.getValue(), aSubFolders.get(filePath), bSubFolders.get(filePath)));

            aSubFolders.remove(filePath);
            bSubFolders.remove(filePath);
        }
        baseSubFolders.clear();

        for (Map.Entry<String, Folder> file : aSubFolders.entrySet()) {
            String filePath = file.getKey();
            subChangesFolders.put(filePath, new FolderChanges(null, file.getValue(), bSubFolders.get(filePath)));

            bSubFolders.remove(filePath);
        }
        aSubFolders.clear();

        for (Map.Entry<String, Folder> file : bSubFolders.entrySet()) {
            String filePath = file.getKey();
            subChangesFolders.put(filePath, new FolderChanges(null, null, file.getValue()));
        }
        bSubFolders.clear();

    }

    private void setHasConflicts(){
        hasConflicts = subChangesFiles.values().stream().anyMatch(fileChanges -> fileChanges.getStatus() == Common.FilesStatus.CONFLICTED);

        if(!hasConflicts)
            hasConflicts = subChangesFolders.values().stream().anyMatch(FolderChanges::getHasConflicts);
    }

    private void setFolderDeleted(){
        if (subChangesFolders.size() > 0)
            folderDeleted = subChangesFolders.values().stream().allMatch((f) ->{return f.getFolderDeleted();});

        if(!folderDeleted) {
            folderDeleted = subChangesFiles.values().stream().allMatch(fileChanges -> fileChanges.getStatus() == Common.FilesStatus.DELETED);
        }
    }

    public boolean getHasConflicts(){return hasConflicts;}
    public boolean getFolderDeleted(){return folderDeleted;}


    public Map<String, FileChanges> getSubChangesFiles(){
        return subChangesFiles;
    }

    public Map<String, FolderChanges> getSubChangesFolders(){
        return subChangesFolders;
    }

    @Override
    boolean commit(String commitUser, String commitTime){
        // function assume the items are up-to-date
        boolean subItemsChanged = false;

        subFiles.clear();
        for(FileChanges file: subChangesFiles.values()){
            if(file.state == Common.FilesStatus.NEW || file.state == Common.FilesStatus.UPDATED || file.state == Common.FilesStatus.RESOLVED ){
                subItemsChanged = true;
                file.updateUserAndDate(commitUser, commitTime);
            }
            else if (file.state == Common.FilesStatus.DELETED){
                subItemsChanged = true;
                continue;
            }
            file.zip();

            file.state = Common.FilesStatus.NO_CHANGE;
            subFiles.put(file.fullPath, file);
        }

        subFolders.clear();
        for(FolderChanges folder: subChangesFolders.values()){
            boolean subFolderChanged = folder.commit(commitUser, commitTime);
            subItemsChanged = subItemsChanged || subFolderChanged;


            if(folder.getFolderDeleted()) {
                continue;
            }

            subFolders.put(folder.fullPath, folder);
        }

        setFolderDeleted();

        if(subItemsChanged)
            updateUserAndDate(commitUser, commitTime);
        else{
            if(!folderDeleted)
                setUserAndDateByLastUpdatedFile();
        }

        setSHA1();
        zip();

        return subItemsChanged;
    }

    @Override
    protected List<Item> getOrderedItemsForSha1(){
        return getOrderedItems(subFiles, subFolders);
    }


    void setUserAndDateByLastUpdatedFile(){

        String user = null;
        String time = null;

        Date maxTime = null;

        for(Blob file: subFiles.values()){
            try{
                Date curTime = Settings.commitDateFormat.parse(file.lastModified);

                if(maxTime == null || (maxTime != null && curTime.after(maxTime))) {
                    maxTime = curTime;
                    user = file.userLastModified;
                    time = file.lastModified;
                }

            }
            catch (ParseException e) {}
        }
        updateUserAndDate(user, time);
    }


    Folder getResFolder(){
        return new Folder(new File(fullPath), currentSHA1, userLastModified, lastModified, repoSettings);
    }

}
