package core;

import java.util.*;

public class FolderChanges extends Folder {

    // TODO maybe need to extend Folder

    Folder baseElement;
    Folder aElement;
    Folder bElement;

    private List<FileChanges> subChangesFiles = new ArrayList<>();
    private List<FolderChanges> subChangesFolders= new ArrayList<>();

    private boolean hasConflicts;



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

    private void setFullPath(){ // TODO it is the same as FilesChanges - merge them somehow
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
            subChangesFiles.add(new FileChanges(file.getValue(), aSubFiles.get(filePath), bSubFiles.get(filePath)));

            aSubFiles.remove(filePath);
            bSubFiles.remove(filePath);
        }
        baseSubFiles.clear();

        for (Map.Entry<String, Blob> file : aSubFiles.entrySet()) {
            String filePath = file.getKey();
            subChangesFiles.add(new FileChanges(null, file.getValue(), bSubFiles.get(filePath)));

            bSubFiles.remove(filePath);
        }
        aSubFiles.clear();

        for (Map.Entry<String, Blob> file : bSubFiles.entrySet()) {
            String filePath = file.getKey();
            subChangesFiles.add(new FileChanges(null, null, file.getValue()));

            bSubFiles.remove(filePath);
        }
    }

    private void setSubFoldersChanges(){
        Map<String, Folder> baseSubFolders = (baseElement != null)? baseElement.getSubFolders(): new HashMap<>();
        Map<String, Folder> aSubFolders = (aElement != null)? aElement.getSubFolders(): new HashMap<>();
        Map<String, Folder> bSubFolders = (bElement != null)? bElement.getSubFolders(): new HashMap<>();

        for (Map.Entry<String, Folder> folder : baseSubFolders.entrySet()) {
            String filePath = folder.getKey();
            subChangesFolders.add(new FolderChanges(folder.getValue(), aSubFolders.get(filePath), bSubFolders.get(filePath)));

            aSubFolders.remove(filePath);
            bSubFolders.remove(filePath);
        }
        baseSubFolders.clear();

        for (Map.Entry<String, Folder> file : aSubFolders.entrySet()) {
            String filePath = file.getKey();
            subChangesFolders.add(new FolderChanges(null, file.getValue(), bSubFolders.get(filePath)));

            bSubFolders.remove(filePath);
        }
        aSubFolders.clear();

        for (Map.Entry<String, Folder> file : bSubFolders.entrySet()) {
            subChangesFolders.add(new FolderChanges(null, null, file.getValue()));
        }
        bSubFolders.clear();

    }

    private void setHasConflicts(){
        hasConflicts = subChangesFiles.stream().anyMatch(fileChanges -> fileChanges.getStatus() == Common.FilesStatus.CONFLICTED);

        if(!hasConflicts)
            hasConflicts = subChangesFolders.stream().anyMatch(FolderChanges::getHasConflicts);
    }

    private boolean getHasConflicts(){return hasConflicts;}

    public List<FileChanges> getSubChangesFiles(){
        return subChangesFiles;
    }

    public List<FolderChanges> getSubChangesFolders(){
        return subChangesFolders;
    }

}
