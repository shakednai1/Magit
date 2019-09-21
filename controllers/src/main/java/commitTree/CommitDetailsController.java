package commitTree;

import core.Commit;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import models.BranchData;
import models.CommitData;
import utils.BaseController;

import java.io.IOException;
import java.util.stream.Collectors;

public class CommitDetailsController extends BaseController {

    public CommitDetailsController(){

    }
    @FXML
    private Label CommitSha1;

    @FXML
    private Label CommitTime;

    @FXML
    private Label CommitMsg;

    @FXML
    private Label CommitAuthor;

    @FXML
    private Label firstPrevCommit;

    @FXML
    private Label SecondPrevCommit;

    @FXML
    private Label includingBranches;

    public void display(CommitData commitData){
            CommitSha1.setText(commitData.getSha1());
            CommitTime.setText(commitData.getCommitTime());
            CommitMsg.setText(commitData.getMessage());
            CommitAuthor.setText(commitData.getCommitter());
            if(commitData.getPreviousCommitSha1() != null && !commitData.getPreviousCommitSha1().equals("null"))
                firstPrevCommit.setText(commitData.getPreviousCommitSha1());
            if(commitData.getSecondPreviousCommitSha1() != null && !commitData.getSecondPreviousCommitSha1().equals("null"))
                SecondPrevCommit.setText(commitData.getSecondPreviousCommitSha1());

//            includingBranches.setText(
//                    commitData.getContainingBranches().stream()
//                    .map(BranchData::getName)
//                    .sorted().collect(Collectors.joining(", "))
//            );
    }
}
