package core;

import java.util.List;
import java.util.stream.Collectors;

public class FilesDelta {

    private List<Blob> newFiles;
    private List<Blob> deletedFiles;
    private List<Blob> updatedFiles;

    public List<Blob> getNewFiles() {
        return newFiles;
    }

    public List<String> getNewFilesPaths() {
        return newFiles.stream().map(Blob::getFullPath).collect(Collectors.toList());
    }

    public void setNewFiles(List<Blob> newFiles) {
        this.newFiles = newFiles;

        for (Blob file: this.newFiles){
            file.setState(Common.FilesStatus.NEW);
        }
    }

    public List<Blob> getDeletedFiles() {
        return deletedFiles;
    }

    public List<String> getDeletedFilesPaths() {
        return deletedFiles.stream().map(Blob::getFullPath).collect(Collectors.toList());
    }

    public void setDeletedFiles(List<Blob> deletedFiles) {

        this.deletedFiles = deletedFiles;
        for (Blob file: this.deletedFiles){
            file.setState(Common.FilesStatus.DELETED);
        }
    }

    public List<Blob> getUpdatedFiles() {
        return updatedFiles;
    }

    public List<String> getUpdatedFilesPaths() {
        return updatedFiles.stream().map(Blob::getFullPath).collect(Collectors.toList());
    }

    public void setUpdatedFiles(List<Blob> updatedFiles) {
        this.updatedFiles = updatedFiles;
        for (Blob file: this.updatedFiles){
            file.setState(Common.FilesStatus.UPDATED);
        }
    }
}
