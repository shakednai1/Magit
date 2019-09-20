package commitTree.node;

import commitTree.CommitDetailsController;
import core.*;
import exceptions.NoActiveRepositoryError;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import models.BranchData;
import sun.applet.Main;
import utils.BaseController;
import workingCopy.WorkingCopyController;
import workingCopy.WorkingCopyStage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommitNodeController extends BaseController {

    @FXML protected Label commitTimeStampLabel;
    @FXML protected Label messageLabel;
    @FXML protected Label committerLabel;
    @FXML protected Circle CommitCircle;
    @FXML public Label pointingBranches;
    String commitSha1;
    String prevCommitSha1 = "";
    String secondPrevCommitSha1 = "";
    MainEngine engine;

    public CommitNodeController(){
        engine = new MainEngine();
    }

    public void setCommitSha1(String commitSha1){
        this.commitSha1 = commitSha1;
    }

    public void setPrevCommitSha1(String prevCommitSha1, String secondPrevCommitSha1){
        this.prevCommitSha1 = prevCommitSha1;
        this.secondPrevCommitSha1 = secondPrevCommitSha1;
    }


    public void setCommitTimeStamp(String timeStamp) {
        commitTimeStampLabel.setText(timeStamp);
        commitTimeStampLabel.setTooltip(new Tooltip(timeStamp));
    }

    public void setCommitter(String committerName) {
        committerLabel.setText(committerName);
        committerLabel.setTooltip(new Tooltip(committerName));
    }

    public void setCommitMessage(String commitMessage) {
        messageLabel.setText(commitMessage);
        messageLabel.setTooltip(new Tooltip(commitMessage));
    }

    public int getCircleRadius() {
        return (int)CommitCircle.getRadius();
    }

    public void setPointingBranches(List<String> branches){
        pointingBranches.setText(String.join(", ", branches));
    }

    public void setContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Get diff from previous commit");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    if (!(prevCommitSha1 == null) && !prevCommitSha1.isEmpty()) {
                        if (secondPrevCommitSha1 == null || secondPrevCommitSha1.isEmpty()) {
                            new WorkingCopyStage().displayCommitDiff(commitSha1, prevCommitSha1);
                        } else {
                            Dialog sha1Dialog = new Dialog();
                            sha1Dialog.setContentText("There are two previous commits for this commit\n please choose one to compare");
                            ButtonType firstSha1Btn = new ButtonType(prevCommitSha1, ButtonBar.ButtonData.APPLY);
                            ButtonType secondSha1Btn = new ButtonType(secondPrevCommitSha1, ButtonBar.ButtonData.APPLY);
                            sha1Dialog.getDialogPane().getButtonTypes().addAll(firstSha1Btn, secondSha1Btn);
                            Optional<ButtonType> result = sha1Dialog.showAndWait();
                            if (result.get() == firstSha1Btn) {
                                new WorkingCopyStage().displayCommitDiff(commitSha1, prevCommitSha1);
                            }
                            if (result.get() == secondSha1Btn) {
                                new WorkingCopyStage().displayCommitDiff(commitSha1, secondPrevCommitSha1);
                            }
                        }
                    }
                    else{
                        new WorkingCopyStage().displayCommitDiff(commitSha1, commitSha1);
                    }
                }
                catch (NoActiveRepositoryError ex){

                }

            }
        });
        MenuItem item2 = new MenuItem("Show File System");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Folder folder = engine.getFileSystemOfCommit(commitSha1);
                FSObject fsObject = new FSObject(folder, true);
                TreeItem<FSObject> root = new TreeItem<>(fsObject);
                buildTreeView(folder, root);
                TreeView treeView = new TreeView();
                treeView.setRoot(root);
                VBox vbox = new VBox(treeView);
                EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
                    handleClick(event);
                };
                treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);                Dialog dialog = new Dialog();
                dialog.setHeaderText("File System");
                dialog.getDialogPane().setContent(vbox);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            }
        });
        MenuItem item3 = new MenuItem("Get Commit Details");
        item3.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                try {
                    FXMLLoader fxmlLoader = openStageReturnFxml("../../commitDetails.fxml");
                    CommitDetailsController controller = fxmlLoader.getController();
                    controller.display(new Commit(commitSha1));

                } catch (IOException ex) {
                    System.out.println("dfsg");
                }
            }});
        contextMenu.getItems().addAll(item1, item2, item3);
        messageLabel.setContextMenu(contextMenu);
    }

    private void handleClick(MouseEvent event) {
        TreeView treeView = (TreeView)event.getSource();
        TreeItem<FSObject> treeItem = (TreeItem<FSObject>)treeView.getSelectionModel().getSelectedItem();
        if (treeItem != null && treeItem.getChildren().isEmpty()){
            FSObject file = treeItem.getValue();
            List<String> fileLines = engine.getFileLines(file.getSha1());
            Dialog dialog = new Dialog();
            dialog.setContentText(String.join("\n", fileLines));
            dialog.setTitle(file.toString());
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        }
    }

    private void buildTreeView(Folder folder, TreeItem<FSObject> root) {
        for(Blob subFile: folder.getSubFiles().values()){
            FSObject fsObject = new FSObject(subFile, false);
            TreeItem<FSObject> file = new TreeItem<>(fsObject);
            root.getChildren().add(file);
        }

        for(Folder subFolder: folder.getSubFolders().values()){
            FSObject fsObject = new FSObject(subFolder, false);
            TreeItem<FSObject> subFolderTreeItem = new TreeItem<>(fsObject);
            root.getChildren().add(subFolderTreeItem);
            buildTreeView(subFolder, subFolderTreeItem);
        }
    }

    public String getCommitTime(){return commitTimeStampLabel.getText();}

}
