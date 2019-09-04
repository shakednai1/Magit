package workingCopy;

import core.Blob;
import core.Common;
import core.FilesDelta;
import core.MainEngine;
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
import java.util.List;

public class WorkingCopyController {

    private MainEngine engine = new MainEngine();
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


}
