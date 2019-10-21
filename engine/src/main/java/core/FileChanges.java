package core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileChanges extends Blob{

    private Blob baseElement;
    private Blob aElement;
    private Blob bElement;
    private Common.FilesStatus aVSbaseStatus;
    private Common.FilesStatus bVSbaseStatus;

    private Blob resElement;

    FileChanges(Blob baseBlob, Blob aBlob, Blob bBlob){
        this.baseElement = baseBlob;
        this.aElement = aBlob;
        this.bElement = bBlob;

        setBasicDetails();

        // two ver has the same sha1
        if(setStateIfDeletedInBothVer()) return;

        // one of the vers has no changes
        aVSbaseStatus = getStatusBetweenVersions(this.baseElement, this.aElement);
        bVSbaseStatus = getStatusBetweenVersions(this.baseElement, this.bElement);

        if (setStateOneOfBlobsNoChange(aVSbaseStatus, bVSbaseStatus)) return;

        if(setStateIfBothVerEqual(aVSbaseStatus)) return;

        setConflicted();
    }

    public Common.FilesStatus getAVSbaseStatus(){
        return aVSbaseStatus;
    }

    public Common.FilesStatus getBVSbaseStatus(){
        return bVSbaseStatus;
    }

    public Blob getaElement(){
        return aElement;
    }
    public Blob getbElement(){
        return bElement;
    }
    public Blob getBaseElement(){
        return baseElement;
    }
    Blob getResElement(){ return resElement; }

    void setResAndStatus(Blob resElement, Common.FilesStatus state){
        this.resElement = resElement;
        this.state = state;

        if (resElement != null){
            currentSHA1 = resElement.currentSHA1;
            userLastModified = resElement.userLastModified;
            lastModified = resElement.lastModified;
        }
    }

    Common.FilesStatus getStatus(){return state;}

    private void setBasicDetails(){
        Blob dataElement = (this.baseElement != null)? this.baseElement :
                (this.aElement != null)? this.aElement:
                        this.bElement;

        this.fullPath = dataElement.fullPath;
        this.name = dataElement.name;
        this.repoSettings = baseElement.repoSettings;
    }


    private boolean setStateIfDeletedInBothVer(){
        if (aElement == null && bElement == null) {
            setResAndStatus(null, Common.FilesStatus.DELETED);
            return true;
        }
        return false;
    }

    private   boolean setStateIfBothVerEqual(Common.FilesStatus optionalState){
        if (aElement == null || bElement == null) return false;
        if (aElement.equals(bElement)){
            setResAndStatus(aElement, optionalState);
            return true;
        }
        return false;
    }

    private boolean setStateOneOfBlobsNoChange(Common.FilesStatus aVSbaseStatus, Common.FilesStatus bVSbaseStatus){
        if (aVSbaseStatus != Common.FilesStatus.NO_CHANGE && bVSbaseStatus != Common.FilesStatus.NO_CHANGE)
            return false;

        if (bVSbaseStatus == Common.FilesStatus.NO_CHANGE)
            setResAndStatus(aElement, aVSbaseStatus);
        else
            setResAndStatus(bElement, bVSbaseStatus);

        return true;
    }

    private void setConflicted(){
        state = Common.FilesStatus.CONFLICTED;
    }

    private Common.FilesStatus getStatusBetweenVersions(Item baseElement, Item newElement){
        if (baseElement == null && newElement == null) return Common.FilesStatus.NO_CHANGE;
        if (baseElement == null && newElement != null) return Common.FilesStatus.NEW;
        if (baseElement != null && newElement == null) return Common.FilesStatus.DELETED;
        if(!baseElement.equals(newElement)) return Common.FilesStatus.UPDATED;
        return Common.FilesStatus.NO_CHANGE;
    }

    @Override
    public String getSha1(){ return resElement.getSha1(); }

    @Override
    public void zip(){
        if(state == Common.FilesStatus.RESOLVED){
            ItemSha1 sha1 = resElement.currentSHA1;
            File resolvedFile = new File(repoSettings.objectsFolderPath, sha1.sha1 + ".txt");
            FSUtils.writeFile(resolvedFile.getAbsolutePath(), sha1.content, false);
            FSUtils.zip(FSUtils.getZippedPath(repoSettings.objectsFolderPath, sha1.sha1),  resolvedFile.getAbsolutePath());
        }
    }

    public void setContent(String text) {
        currentSHA1 = new ItemSha1(text, true, true, repoSettings.getRepositoryObjectsFullPath());
        state = Common.FilesStatus.RESOLVED;

        resElement = new Blob(new File(fullPath), currentSHA1, "", "", repoSettings);
    }

    public void markDeleted() {
        state = Common.FilesStatus.DELETED;
    }

    public void rewriteFS() {
        if (state == Common.FilesStatus.DELETED)
            FSUtils.deleteFile(fullPath);
        else if (state == Common.FilesStatus.UPDATED || state == Common.FilesStatus.NEW || state == Common.FilesStatus.RESOLVED) {
            FSUtils.createNewFile(fullPath, resElement.getContent());
        }
    }
}
