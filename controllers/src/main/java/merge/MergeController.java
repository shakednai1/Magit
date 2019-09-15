package merge;
import core.Blob;
import core.FileChanges;
import core.MainEngine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.BaseController;

public class MergeController extends BaseController {

        FileChanges conflict;
        Stage stage;

        boolean resolved;

        @FXML
        private TextArea oursContent;

        @FXML
        private Label oursBranch;

        @FXML
        private Label baseCommit;

        @FXML
        private TextArea baseContent;

        @FXML
        private Label theirsBranch;

        @FXML
        private TextArea theirsContent;

        @FXML
        private Button submitConflictBtn;

        @FXML
        private Button cancelBtn;

        @FXML
        private RadioButton markAsDeleted;

        @FXML
        private TextArea resultContent;

        @FXML
        void OnCancelBtn(ActionEvent event) {
            resolved = false;
            stage.close();
        }

        @FXML
        void OnResolveConflict(ActionEvent event) {

            if(markAsDeleted.isSelected())
                conflict.markDeleted();
            else
                conflict.setContent(resultContent.getText());
            resolved=true;
            stage.close();
        }

        void execute(FileChanges fileChanges, Stage stage){
            this.conflict = fileChanges;
            this.stage = stage;

            Blob aElement =  fileChanges.getaElement();
            Blob baseElement =  fileChanges.getBaseElement();
            Blob bElement =  fileChanges.getbElement();

            if(aElement != null)
                oursContent.setText(aElement.getContent());
            if(baseElement != null)
                baseContent.setText(baseElement.getContent());
            if(bElement != null)
                theirsContent.setText(bElement.getContent());

            oursBranch.setText(MainEngine.getCurrentBranchName());
            theirsBranch.setText(MainEngine.getBranchMergeName());

        }



}




