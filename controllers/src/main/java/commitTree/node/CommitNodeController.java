package commitTree.node;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import models.BranchData;
import java.util.List;

public class CommitNodeController {

    @FXML protected Label commitTimeStampLabel;
    @FXML protected Label messageLabel;
    @FXML protected Label committerLabel;
    @FXML protected Circle CommitCircle;
    @FXML protected Label pointingBranches;

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
                System.out.println("diff");
            }
        });
        MenuItem item2 = new MenuItem("Show File System");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("filesystem");
            }
        });
        contextMenu.getItems().addAll(item1, item2);
        messageLabel.setContextMenu(contextMenu);
    }

    public String getCommitTime(){return commitTimeStampLabel.getText();}

}
