import exceptions.InvalidBranchNameError;
import exceptions.NoActiveRepositoryError;
import exceptions.UncommittedChangesError;
import exceptions.XmlException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class appController {

    private static MainEngine engine ;

    public appController(){
        engine = new MainEngine();
    }

    @FXML
    private MenuItem createNewBranch;

    @FXML
    private MenuItem checkoutBranch;

    @FXML
    private MenuItem deleteBranch;

    @FXML
    private Label currentRepo;

    @FXML
    private Label currentUser;

    @FXML
    private Label currentBranch;

    @FXML
    private Button switchUser;

    @FXML
    private MenuButton repoOptions;

    @FXML
    private MenuItem createNewRepo;

    @FXML
    private MenuItem switchRepo;

    @FXML
    private MenuItem loadFromXml;


    @FXML
    void OnCheckoutBranch(ActionEvent event) {
        String branchNameToCheckout = null;
        try{
            List<String> branchesName = getAllBranchesName();
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
            choiceDialog.setContentText("Choose branch to checkout");
            choiceDialog.setHeaderText("Checkout branch");
            Optional<String> branchToCheckout = choiceDialog.showAndWait();
            branchNameToCheckout = branchToCheckout.get();
            engine.checkoutBranch(branchNameToCheckout, false);
        }
        catch (InvalidBranchNameError | NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            // InvalidBranchNameError cannot happen !
        }
        catch (UncommittedChangesError e){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Uncommited changes in current branch");
            alert.setHeaderText("Given Branch has open changes. you need to commit then or force checkout and the changes will remove ");
            alert.setContentText("Are you sure you want to force commit ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                try {
                    engine.checkoutBranch(branchNameToCheckout, true);
                }
                catch (InvalidBranchNameError | UncommittedChangesError | NoActiveRepositoryError ex){
                }
            }
        }
    }


    @FXML
    void OnCreateNewBranchPopUp(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        dialog.setTitle("Create new branch");
        dialog.setHeaderText("Enter branch name");
        dialog.setContentText("Name");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).visibleProperty().set(false);

        Validator validBranch = (value) -> {
            try {
                engine.createNewBranch(value, false);
                return true;
            }
            catch (InvalidBranchNameError | UncommittedChangesError | NoActiveRepositoryError e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
            return false;
        };

        dialog.show();

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                OKevent -> {
                    if (!validBranch.isValid(dialog.getContentText())) {
                        OKevent.consume();
                    }
                }
        );
    }

    @FXML
    void OnDeleteBranch(ActionEvent event) {
        try{
           List<String> branchesName = getAllBranchesName();
           ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
           choiceDialog.setContentText("Choose branch to delete");
           choiceDialog.setHeaderText("Delete branch");
           Optional<String> branchToDelete = choiceDialog.showAndWait();
           engine.deleteBranch(branchToDelete.get());
        }
        catch (InvalidBranchNameError | NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            // InvalidBranchNameError cannot happen !
        }
        catch (IllegalArgumentException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Given branch name is the head branch. Cannot delete head branch! ");
            alert.showAndWait();

        }
    }

    @FXML
    void OnLoadFromXml(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(MyApp.stage);
        try{
            engine.isXmlValid(selectedFile.getPath());
            engine.loadRepositoyFromXML();
            currentRepo.setText("Current Repo: " + engine.getCurrentRepoName());
        }
        catch (XmlException | UncommittedChangesError | InvalidBranchNameError | NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void OnSwitchRepo(ActionEvent event) {

    }

    @FXML
    void OnSwitchUserPopUp(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Switch User");
        dialog.setHeaderText("Enter user name:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            engine.changeCurrentUser(result.get());
            currentUser.setText("Current User: " + result.get());
        }
    }

    @FXML
    void OnCreateNewRepo(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File dir = directoryChooser.showDialog(MyApp.stage);
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        String newRepoPath = dir.getPath() +"/" + result.get();
        dialog.setTitle("Repository name");
        dialog.setHeaderText("Enter repository name:");
        dialog.setContentText("Name:");
        Optional<String> repoName = dialog.showAndWait();
        String newRepoName = repoName.get();
        engine.createNewRepository(newRepoPath, newRepoName);
    }

    private List<String> getAllBranchesName() throws NoActiveRepositoryError{
        List<String> branches = engine.getAllBranches();
        List<String> branchesName = new ArrayList<>(branches.size());
        for(String branch : branches){
            String[] splited = branch.split(",");
            String name = splited[0];
            if (splited[0].contains("HEAD")){
                String[] splitedHead = splited[0].split("\\* ");
                name = splitedHead[splitedHead.length - 1];
            }
            branchesName.add(name);
        }
        return branchesName;
    }

}
