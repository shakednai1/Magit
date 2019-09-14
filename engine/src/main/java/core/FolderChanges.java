package core;

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

        setFullPath();

        setSubFilesChanges();
        setSubFoldersChanges();

        setHasConflicts();
        // at this point, no sha1 need to be calc. only at commit
    }

    private void setFullPath(){ // TODO it is the same as FilesChanges - Merge them somehow
        this.fullPath = (this.baseElement != null)? this.baseElement.fullPath :
                (this.aElement != null)? this.aElement.fullPath:
                        this.bElement.fullPath;
    }

    private void setSubFilesChanges() {
        Map<String, Blob> baseSubFiles = (baseElement != null)? baseElement .getCommittedFilesState(true): new HashMap<>();
        Map<String, Blob> aSubFiles = (aElement != null)? aElement.getCommittedFilesState(true): new HashMap<>();
        Map<String, Blob> bSubFiles = (bElement != null)? bElement.getCommittedFilesState(true): new HashMap<>();

        for (Map.Entry<String, Blob> file : baseSubFiles.entrySet()) {
            String filePath = file.getKey();
            subChangesFiles.put(fullPath, new FileChanges(file.getValue(), aSubFiles.get(filePath), bSubFiles.get(filePath)));

            aSubFiles.remove(filePath);
            bSubFiles.remove(filePath);
        }
        baseSubFiles.clear();

        for (Map.Entry<String, Blob> file : aSubFiles.entrySet()) {
            String filePath = file.getKey();
            subChangesFiles.put(filePath, new FileChanges(null, file.getValue(), bSubFiles.get(filePath)));

            bSubFiles.remove(filePath);
        }
        aSubFiles.clear();

        for (Map.Entry<String, Blob> file : bSubFiles.entrySet()) {
            String filePath = file.getKey();
            subChangesFiles.put(filePath, new FileChanges(null, null, file.getValue()));

            bSubFiles.remove(filePath);
        }
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
        folderDeleted = subChangesFolders.values().stream().allMatch(FolderChanges::getFolderDeleted);

        if(folderDeleted) {
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

    public void unfoldFS(){
        subChangesFiles.values().forEach(FileChanges::rewriteFS);
        subChangesFolders.values().forEach(FolderChanges::unfoldFS);
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
                continue;
            }
            file.zip();
            subFiles.put(file.fullPath, (Blob) file);
        }

        subFolders.clear();
        for(FolderChanges folder: subChangesFolders.values()){
            boolean subFolderChanged = folder.commit(commitUser, commitTime);
            subItemsChanged = subItemsChanged || subFolderChanged;

            setFolderDeleted();

            if(folder.getFolderDeleted()) {
                continue;
            }
            subFolders.put(fullPath, (Folder) folder);
        }

        if(subItemsChanged)
            updateUserAndDate(commitUser, commitTime);

        setSHA1();
        zip();

        return subItemsChanged;
    }

    @Override
    protected List<Item> getOrderedItemsForSha1(){
        return getOrderedItems(subFiles, subFolders);
    }

}
