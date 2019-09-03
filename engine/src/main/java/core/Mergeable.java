package core;

abstract public class Mergeable implements Zipable {

    Item baseElement;
    Item aElement;
    Item bElement;

    Item resElement;

    Common.FilesStatus status;

    protected boolean setStateIfDeletedInBothVer(){
        if (aElement == null && bElement == null) {
            setResAndStatus(null, Common.FilesStatus.DELETED);
            return true;
        }
        return false;
    }

    protected  boolean setStateIfBothVerEqual(Common.FilesStatus optionalState){
        if (aElement == null || bElement == null) return false;
        if (aElement.equals(bElement)){
            setResAndStatus(aElement, optionalState);
            return true;
        }
        return false;
    }

    protected  boolean setStateOneOfBlobsNoChange(Common.FilesStatus aVSbaseStatus, Common.FilesStatus bVSbaseStatus){
        if (aVSbaseStatus != Common.FilesStatus.NO_CHANGE && bVSbaseStatus != Common.FilesStatus.NO_CHANGE)
            return false;

        if (bVSbaseStatus == Common.FilesStatus.NO_CHANGE)
            setResAndStatus(aElement, aVSbaseStatus);
        else
            setResAndStatus(bElement, bVSbaseStatus);

        return true;
    }

    protected  void setConflicted(){
        status = Common.FilesStatus.CONFLICTED;
    }

    protected  Common.FilesStatus getStatusBetweenVersions(Item baseElement, Item newElement){
        if (baseElement == null && newElement == null) return null;
        if (baseElement == null && newElement != null) return Common.FilesStatus.NEW;
        if (baseElement != null && newElement == null) return Common.FilesStatus.DELETED;
        if(!baseElement.equals(newElement)) return Common.FilesStatus.UPDATED;
        return Common.FilesStatus.NO_CHANGE;
    }

    abstract void setResAndStatus(Item resElement, Common.FilesStatus state);

    @Override
    public String getSha1(){ return resElement.currentSHA1.sha1; }

    @Override
    public void zip(){ resElement.zip(); }


}
