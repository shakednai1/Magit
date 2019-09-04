package commitTree.node;

import core.Blob;
import core.Folder;
import core.MainEngine;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import models.BranchData;
import sun.applet.Main;

import java.util.List;
import java.util.Map;

public class CommitNodeController {

    @FXML protected Label commitTimeStampLabel;
    @FXML protected Label messageLabel;
    @FXML protected Label committerLabel;
    @FXML protected Circle CommitCircle;
    @FXML protected Label pointingBranches;
    String commitSha1;
    MainEngine engine;

    public CommitNodeController(){
        engine = new MainEngine();
    }

    public void setCommitSha1(String commitSha1){
        this.commitSha1 = commitSha1;
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

    // TODO double click to show file content
    // TODO use WC status to display diff between commits
    public void setContextMenu(){
        ContextMenu contextMenu = new ContextMenu();
        MenuItem item1 = new MenuItem("Get diff from previous commit");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                engine.getDiffBetweenCommits(commitSha1);
            }
        });
        MenuItem item2 = new MenuItem("Show File System");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Folder folder = engine.getFileSystemOfCommit(commitSha1);
                TreeItem<String> root = new TreeItem<String>(folder.getFullPath());
                buildTreeView(folder, root);
                TreeView treeView = new TreeView();
                treeView.setRoot(root);
                VBox vbox = new VBox(treeView);
                Dialog dialog = new Dialog();
                dialog.setHeaderText("File System");
                dialog.getDialogPane().setContent(vbox);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            }
        });
        contextMenu.getItems().addAll(item1, item2);
        messageLabel.setContextMenu(contextMenu);
    }

    private void buildTreeView(Folder folder, TreeItem<String> root) {
        for(Blob subFile: folder.getSubFiles().values()){
            TreeItem<String> subFileTreeItem = new TreeItem(subFile.getName());
            root.getChildren().add(subFileTreeItem);
        }

        for(Folder subFolder: folder.getSubFolders().values()){
            TreeItem<String> subFolderTreeItem = new TreeItem(subFolder.getName());
            root.getChildren().add(subFolderTreeItem);
            buildTreeView(subFolder, subFolderTreeItem);
        }
    }

    public String getCommitTime(){return commitTimeStampLabel.getText();}

}
