import exceptions.InvalidBranchNameError;
import exceptions.NoActiveRepositoryError;
import exceptions.UncommittedChangesError;
import exceptions.XmlException;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import models.RepositoryModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppController {

    private static MainEngine engine ;
    private static RepositoryModel repositoryModel;

    public AppController(){
        engine = new MainEngine();
        repositoryModel = new RepositoryModel();

    }

    public void setBindings(){
        currentRepo.textProperty().bind(Bindings.format("%s > %s",
                repositoryModel.getRepoNameProperty(),
                repositoryModel.getRepoPathProperty()));

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
            updateBranch();
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
            alert.setContentText("Are you sure you want to force checkout ?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                try {
                    engine.checkoutBranch(branchNameToCheckout, true);
                    updateBranch();
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
            updateCurrentRepo(engine.getCurrentRepoPath());
        }
        catch (XmlException | UncommittedChangesError | InvalidBranchNameError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        catch (NoActiveRepositoryError e){

        }
    }

    @FXML
    void OnSwitchRepo(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File dir = directoryChooser.showDialog(MyApp.stage);
        if(!engine.changeActiveRepository(dir.getPath())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("path is not contains .magit folder!");
            alert.showAndWait();
        }
        else{
            updateCurrentRepo(dir.getPath());
        }
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
        String newRepoPath = dir.getPath() +"/" + repositoryFolderNameDialog();
        String newRepoName = repositoryNameDialog();
        engine.createNewRepository(newRepoPath, newRepoName);
    }

    @FXML
    void OnWCStatus(ActionEvent event){
        try{
            Map<String, List<String>> changes = engine.getWorkingCopyStatus();
            List<String> updatedFiles = changes.get("update");
            List<String> newFiles = changes.get("new");
            List<String> deletedFiles = changes.get("delete");
            String files = "";
            files = files.concat("Update: \n");
            for(String updatedFile : updatedFiles){
                files = files.concat(updatedFile + "\n");
            }
            files = files.concat("\n");
            files = files.concat("\n");

            files = files.concat("New: \n");
            for(String newFile : newFiles){
                files = files.concat(newFile + "\n");
            }
            files = files.concat("\n");
            files = files.concat("\n");

            files = files.concat("Deleted: \n");
            for(String deletedFile : deletedFiles){
                files = files.concat(deletedFile + "\n");
            }
            files = files.concat("\n");
            files = files.concat("\n");

            Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
            alertInfo.setContentText(files);
            alertInfo.setHeaderText("Working copy status:");
            alertInfo.setTitle("WC status");
            alertInfo.showAndWait();
        }
        catch (NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    void OnCommit(ActionEvent event){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Commit");
        dialog.setHeaderText("Enter commit message:");
        dialog.setContentText("Message:");
        Optional<String> commitMsg = dialog.showAndWait();
        try{
            if(!engine.commit(commitMsg.get())){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("There are no changes to commit");
                alert.showAndWait();
            }
        }
        catch (NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void OnListAllBranches(){
        try {
            List<String> allBranches = getAllBranchesName();
            Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
            alertInfo.setTitle("Branches");
            alertInfo.setHeaderText("All Branches:");
            String str = "";
            for(String branch : allBranches){
                if(engine.getCurrentBranchName().equals(branch)){
                    str = str.concat("**HEAD** ");
                }
                str = str.concat(branch + "\n");

            }
            alertInfo.setContentText(str);
            alertInfo.showAndWait();
        }
        catch (NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }

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

    private void updateCurrentRepo(String repoPath){
        try{
            repositoryModel.setRepo(engine.getCurrentRepoName(), repoPath);
            updateBranch();
        }
        catch (NoActiveRepositoryError e){

        }
    }

    private void updateBranch(){
        currentBranch.setText("Current Branch: " + engine.getCurrentBranchName());
    }

    RepositoryModel getRepositoryModel(){ return repositoryModel; }

    @FXML
    void OnResetBranch(ActionEvent event) {
        try{
            if(engine.getActiveBranch().haveChanges()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Uncommited changes in current branch");
                alert.setHeaderText("Given Branch has open changes. you need to commit then or force reset and the changes will remove ");
                alert.setContentText("Are you sure you want to reset and the changes will not saved ?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    showResetBranchDialog();
                }
            }
            else {
                showResetBranchDialog();
            }
        }
        catch (NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void showResetBranchDialog(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Reset Branch");
        dialog.setHeaderText("Enter commit SHA1:");
        dialog.setContentText("SHA1:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            engine.resetBranch(result.get());
            try {
                engine.getActiveBranch().getRootFolder().updateState();
            }
            catch (NoActiveRepositoryError e){

            }
        }
    }

    @FXML
    void OnCloneRepo(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select source repository location ");
        File SrcDir = directoryChooser.showDialog(MyApp.stage);
        DirectoryChooser directoryChooser2 = new DirectoryChooser();
        directoryChooser2.setTitle("Select destination repository location ");
        File DstDir = directoryChooser2.showDialog(MyApp.stage);
        String repoFolderName = repositoryFolderNameDialog();
        String repoName = repositoryNameDialog();
        engine.cloneRepo(SrcDir.getPath(), DstDir.getPath() + repoFolderName, repoName);
        updateCurrentRepo(DstDir.getPath() + repoFolderName);
    }

    @FXML
    void OnFetch(ActionEvent event){
        try {
            engine.fetchRepo();
        }
        catch (IllegalArgumentException e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    String repositoryFolderNameDialog(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        return result.get();
    }

    String repositoryNameDialog(){
        TextInputDialog dialog2 = new TextInputDialog("");
        dialog2.setTitle("Repository name");
        dialog2.setHeaderText("Enter repository name:");
        dialog2.setContentText("Name:");
        Optional<String> repoName = dialog2.showAndWait();
        return repoName.get();
    }

}
