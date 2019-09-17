package commitTree;

import core.Commit;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import utils.BaseController;

import java.io.IOException;

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

    public void display(Commit commit){
            CommitSha1.setText(commit.getSha1());
            CommitTime.setText(commit.getCommitTime());
            CommitMsg.setText(commit.getMsg());
            CommitAuthor.setText(commit.getUserLastModified());
            if(commit.getFirstPrecedingSha1() != null && !commit.getFirstPrecedingSha1().equals("null"))
                firstPrevCommit.setText(commit.getFirstPrecedingSha1());
            if(commit.getSecondPrecedingSha1() != null && !commit.getSecondPrecedingSha1().equals("null"))
                SecondPrevCommit.setText(commit.getSecondPrecedingSha1());


    }
}
