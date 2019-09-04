package commitTree.node;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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

}
