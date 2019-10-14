package workingCopy;

import core.*;
import exceptions.NoActiveRepositoryError;
import exceptions.NoChangesToCommitError;
import fromXml.MagitSingleBranch;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkingCopyController {

    private MainEngine engine;
    private FilesDelta filesDelta;

    @FXML ListView<Blob> filesListView;

    public void initialize(){
        filesListView.setCellFactory(new Callback<ListView<Blob>, ListCell<Blob>>() {
            @Override
            public ListCell<Blob> call(ListView param) {
                return new WorkingCopyCell();
            }
        });
    }

    public void setFilesDelta() throws NoActiveRepositoryError{
        filesDelta = engine.getWorkingCopyStatus();

        ObservableList<Blob> items= FXCollections.observableArrayList(filesDelta.getNewFiles());
        items.addAll(filesDelta.getDeletedFiles());
        items.addAll(filesDelta.getUpdatedFiles());

        filesListView.setItems(items);
    }

    public void setFilesDeltaCommit(String commitSha1, String prevCommit){
        FolderChanges folderChanges = CommitsDelta.getDiffBetweenCommits(commitSha1, prevCommit, null);
        List<Blob> deletedFiles = new LinkedList<>();
        List<Blob> updatedFiles = new LinkedList<>();
        List<Blob> newFiles = new LinkedList<>();
        getFilesState(deletedFiles, updatedFiles, newFiles, folderChanges);

        ObservableList<Blob> items= FXCollections.observableArrayList(newFiles);
        items.addAll(deletedFiles);
        items.addAll(updatedFiles);

        filesListView.setItems(items);

    }

    public void getFilesState(List<Blob> deletedFiles, List<Blob> updatedFiles, List<Blob> newFiles, FolderChanges folder){
        for(FileChanges file : folder.getSubChangesFiles().values()){
            Common.FilesStatus status = file.getState();
            if(status == Common.FilesStatus.NEW) {
                newFiles.add(file);
            }
            else if(status == Common.FilesStatus.UPDATED) {
                updatedFiles.add(file);
            }
            else if(status == Common.FilesStatus.DELETED) {
                deletedFiles.add(file);
            }
        }
        for(FolderChanges subFolder: folder.getSubChangesFolders().values()){
            getFilesState(deletedFiles, updatedFiles, newFiles, subFolder);
        }
    }


}
