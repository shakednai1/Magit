import commitTree.CommitTree;
import core.*;
import exceptions.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import merge.ConflictFileCell;
import models.BranchData;
import models.CommitData;
import models.RepositoryModel;
import utils.BaseController;
import workingCopy.WorkingCopyCell;
import workingCopy.WorkingCopyStage;

import java.io.File;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.*;
import java.util.stream.Collectors;

public class AppController extends BaseController {

    private MainEngine engine ;
    private RepositoryModel repositoryModel;
    private CommitTree commitTree;
    private ObservableMap<String, CommitData> commits;
    private ObservableList<BranchData> branches;

    // TODO active branch property, relate current core.Branch label


    public AppController(){
        engine = new MainEngine();
        repositoryModel = new RepositoryModel();
    }

    public void initialize(){
        currentRepo.textProperty().bind(Bindings.format("%s > %s",
                repositoryModel.getRepoNameProperty(),
                repositoryModel.getRepoPathProperty()));

        commitTree = new CommitTree();
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

    @FXML ScrollPane commitTreeScroll;


    public class BranchHeadCommitChangedListener implements ChangeListener<String>{
        private BranchData branchData;

        BranchHeadCommitChangedListener(BranchData branchData){this.branchData = branchData;}

        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            commits.get(oldValue).removePointingBranch(branchData);
            commits.get(newValue).addPointingBranch(branchData);
        }

    }



    CommitTree getCommitTree(){ return commitTree; }

    @FXML
    void OnCheckoutBranch(ActionEvent event) {
        boolean checkout = true;
        String branchNameToCheckout = null;
        try{
            List<String> branchesName = getAllBranchNames();
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
            choiceDialog.setContentText("Choose branch to checkout");
            choiceDialog.setHeaderText("Checkout branch");
            Optional<String> branchToCheckout = choiceDialog.showAndWait();
            branchNameToCheckout = branchToCheckout.get();
            if(branchNameToCheckout.contains("/")){
                Alert alert = new Alert(Alert.AlertType.NONE);
                alert.setHeaderText("Checkout to remote branch");
                alert.setTitle("Checkout to remote branch");
                alert.setContentText("Cannot checkout to remote branch\nDo you want to create tracking branch instead and checkout?");
                ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.APPLY);
                ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
                alert.getDialogPane().getButtonTypes().addAll(yes, no);
                Optional<ButtonType> result = alert.showAndWait();
                if(result.get() == yes){
                    String branchName = branchNameToCheckout.split("/")[1];
                    String sha1 = engine.getSha1FromRemoteBranch(branchName);
                    engine.createNewBranchFromSha1(branchName, sha1, true);
                    branchNameToCheckout = branchName;
                }
                else{
                    checkout = false;
                }

            }
            if(checkout){
                engine.checkoutBranch(branchNameToCheckout, false);
                updateBranch();

            }
        }
        catch (NoActiveRepositoryError e){
            showErrorAlert(e);
        }
        catch (InvalidBranchNameError e){
            // InvalidBranchNameError cannot happen !
        }

        catch (UncommittedChangesError e){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Uncommited changes in current branch");
            alert.setHeaderText("Given core.Branch has open changes. you need to commit them or force checkout and the changes will remove ");
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

        try{
            Stage nbStage = openStage("newBranchPopUp.fxml");
            nbStage.setTitle("Create New Branch");

            AnchorPane root = (AnchorPane) nbStage.getScene().getRoot();

            final Button btCancel =  (Button) root.lookup("#cancelNewBranch");
            btCancel.addEventFilter(ActionEvent.ACTION, _event -> {nbStage.close();});

            final Button btOk =  (Button) root.lookup("#okNewBranch");
            btOk.addEventFilter(
                    ActionEvent.ACTION,
                    OKevent -> {

                        String branchName = ((TextField) root.lookup("#branchNameField")).textProperty().getValue();
                        boolean checkout = ((CheckBox) root.lookup("#checkoutCheckbox")).isSelected();

                        try {
                            createNewBranch(branchName, checkout);
                        }
                        catch (InvalidBranchNameError | UncommittedChangesError | NoActiveRepositoryError e) {
                            showErrorAlert(e);
                        }
                        finally {
                            OKevent.consume();
                            nbStage.close();
                        }
                    }
                    );
        }
        catch (IOException e){
            System.out.println("Error at creating new branch: " );
            e.printStackTrace();
        }
    }

    private void createNewBranch(String branchName, boolean checkout) throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError{
        BranchData branchData = engine.createNewBranch(branchName, checkout);
        branchData.getHeadSha1Property().addListener(new BranchHeadCommitChangedListener(branchData));

        // TODO - make active branch as property
        if(checkout)
            updateBranch();
    }

    @FXML
    void OnDeleteBranch(ActionEvent event) {
        try{
           List<String> branchesName = getAllBranchNames();
           ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
           choiceDialog.setContentText("Choose branch to delete");
           choiceDialog.setHeaderText("Delete branch");
           Optional<String> branchToDelete = choiceDialog.showAndWait();
           deleteBranch(branchToDelete.get());
        }
        catch (NoActiveRepositoryError e){
            showErrorAlert(e);
        }
        catch(InvalidBranchNameError e){
            // InvalidBranchNameError cannot happen !
        }
        catch (IllegalArgumentException e){
            showErrorAlert(new Exception("Given branch name is the head branch. Cannot delete head branch! "));
        }
    }

    private void deleteBranch(String branchName) throws NoActiveRepositoryError, InvalidBranchNameError, IllegalArgumentException {
        engine.deleteBranch(branchName);
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
            Task task = new LoadXmlTask(selectedFile.getPath(), engine);
            new Thread(task).start();
            task.setOnSucceeded(e -> {
                try{
                    updateCurrentRepo(engine.getCurrentRepoPath());
                }
                catch (NoActiveRepositoryError ex){

                }
            });
        }
        catch (XmlException e){
            showErrorAlert(e);
        }

    }

    @FXML
    void OnSwitchRepo(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select repository location ");
        File dir = directoryChooser.showDialog(MyApp.stage);
        if(!engine.changeActiveRepository(dir.getPath())){
            showErrorAlert(new Exception("path is not contains .magit folder!"));
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
            // TODO - add icon for deleted, new, updated
            new WorkingCopyStage().displayWCstatus();
        }
        catch (NoActiveRepositoryError e){ showErrorAlert(e); }
    }

    @FXML
    void OnCommit(ActionEvent event){

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Commit");
        dialog.setHeaderText("Enter commit message:");
        dialog.setContentText("Message:");
        Optional<String> commitMsg = dialog.showAndWait();
        try{
            try{
                engine.commit(commitMsg.get());
            }
            catch (NoChangesToCommitError e){
                showErrorAlert(new Exception("There are no changes to commit"));
            }
        }
        catch (NoActiveRepositoryError e){ showErrorAlert(e); }
    }

    @FXML
    public void OnTemp(){
        String sha1a = "f1d7620781290535490b8164d753e7a5052a944e";
        String sha1b = "680ffa2139deeb70f91c2c007c2de0942ddb9818";
        CommitsDelta diff = new CommitsDelta(sha1b, sha1a);
        diff.calcFilesMergeState();

    }

    @FXML
    public void OnListAllBranches(){
        try {
            List<String> allBranches = getAllBranchNames();
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
            showErrorAlert(e);
        }

    }

    private void updateCurrentRepo(String repoPath){
        try{
            repositoryModel.setRepo(engine.getCurrentRepoName(), repoPath);
            updateBranch();

            branches = engine.getAllBranches();

            commits = engine.getAllCommitsData();
            commitTree.setCommitsTree(commits);
            commits.addListener(new MapChangeListener<String, CommitData>() {
                @Override
                public void onChanged(Change<? extends String, ? extends CommitData> change) {
                    if(change.wasAdded())
                        commitTree.addCommit(change.getValueAdded());
                }
            });
        }
        catch (NoActiveRepositoryError e){

        }
    }

    private void updateBranch(){
        currentBranch.setText("Current Branch: " + engine.getCurrentBranchName());
    }


    private List<String> getAllBranchNames() throws NoActiveRepositoryError{
        List<String> branchesNames = engine.getAllBranches().stream().map((branch) -> branch.getName()).collect(Collectors.toList());
        branchesNames.addAll(engine.getAllRemoteBranchesName());
        return branchesNames;
    }

    private void showErrorAlert(Exception e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    @FXML
    void OnResetBranch(ActionEvent event) {
        try{
            if(engine.getActiveBranch().haveChanges()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Uncommited changes in current branch");
                alert.setHeaderText("Given core.Branch has open changes. you need to commit then or force reset and the changes will remove ");
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
        dialog.setTitle("Reset core.Branch");
        dialog.setHeaderText("Enter commit SHA1:");
        dialog.setContentText("SHA1:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            engine.resetBranch(result.get());
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
        catch (IllegalArgumentException | NoActiveRepositoryError e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void OnCreateBranchFromSha1(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Create new branch from sha1");
        dialog.setHeaderText("Enter sha1");
        dialog.setContentText("SHA1");
        Optional<String> sha1 = dialog.showAndWait();
        String tracking = engine.findTrackingAfterBySha1(sha1.get());
        if(tracking!= null) {
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setHeaderText("Tracking after remote branch");
            alert.setTitle("Tracking after remote branch");
            alert.setContentText("This SHA1 pointed by remote branch\ndo you want to create remote branch?");
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.APPLY);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
            alert.getDialogPane().getButtonTypes().addAll(yes, no);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == yes) {
                engine.createNewBranchFromSha1(tracking, sha1.get(), true);
            } else if (result.get() == no) {
                String branchName = showCreateBranchDialog();
                engine.createNewBranchFromSha1(branchName, sha1.get(), false);

            }
        }
        else{
            String branchName = showCreateBranchDialog();
            engine.createNewBranchFromSha1(branchName, sha1.get(), false);
        }

    }

    @FXML
    void OnMerge(ActionEvent event) {
        try {
            if(!canExecuteMerge()) {
                showErrorAlert(new Exception("You have open changes. \n Please commit/reset them before merge"));
                return;
            };

            Merge merge = engine.getActiveRepository().getMerge(getBranchToMerge());
            handleConflicts(merge);
        }
        catch(NoActiveRepositoryError e){
                showErrorAlert(e);
            }

    }

    void handleConflicts(Merge merge) {
        if (merge.getConflicts().size() != 0) {
            ListView listView = new ListView();
            listView.setCellFactory(new Callback<ListView<FileChanges>, ListCell<FileChanges>>() {
                @Override
                public ListCell<FileChanges> call(ListView param) {
                    return new ConflictFileCell();
                }
            });
            ObservableList<FileChanges> items = FXCollections.observableList(merge.getConflicts());
            listView.setItems(items);
            AnchorPane anchorPane = new AnchorPane();
            anchorPane.getChildren().add(listView);
            Stage stage = new Stage();
            stage.setScene(new Scene(anchorPane));
            stage.show();
            items.addListener(new ListChangeListener() {

                public void onChanged(Change change) {
                    if (!change.getList().isEmpty()) return;
                    stage.close();

                    TextInputDialog d = new TextInputDialog();

                    d.setTitle("Commit getMerge");
                    d.setContentText("Enter getMerge commit message");
                    d.showAndWait();
                    // TODO cannot be an empty getMerge msg
                    merge.setCommitMsg(d.getResult());
                    engine.getActiveRepository().makeMerge(merge);
                }
            });
        }
    }

    @FXML
    void OnPull(ActionEvent event) {
        if(!engine.getCanPull()) showErrorAlert(new Exception("You have commits that not pushed yet \n Please push first and then pull"));
        pull();
    }

    void pull(){
        try{
            if(!canExecuteMerge()) showErrorAlert(new Exception("You have open changes. \n Please commit/reset them before merge"));
            Merge merge = engine.pull();
            handleConflicts(merge);
            }
            catch (IllegalArgumentException | NoActiveRepositoryError e) {
                showErrorAlert(e);

        }
    }

    @FXML
    void OnPush(ActionEvent event) {
        pull();
        engine.push();
    }

    private boolean canExecuteMerge() throws NoActiveRepositoryError {
        return !engine.getActiveBranch().haveChanges();
    }


    private String getBranchToMerge() throws NoActiveRepositoryError{
        List<String> branchesName = getAllBranchNames();
        branchesName.remove(engine.getCurrentBranchName());

        ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
        choiceDialog.setContentText("Choose branch to Merge with the head branch");
        choiceDialog.setHeaderText("Merge branch");
        Optional<String> branchToMerge = choiceDialog.showAndWait();
        return branchToMerge.get();
    }

    public String showCreateBranchDialog(){
        TextInputDialog BranchDialog = new TextInputDialog("");
        BranchDialog.setTitle("Create new branch from sha1");
        BranchDialog.setHeaderText("Enter branch name");
        BranchDialog.setContentText("Name");
        Optional<String> branchName = BranchDialog.showAndWait();
        return branchName.get();
    }

    String repositoryFolderNameDialog(){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("core.Repository folder name");
        dialog.setHeaderText("Enter repository folder name:");
        dialog.setContentText("Name:");
        Optional<String> result = dialog.showAndWait();
        return result.get();
    }

    String repositoryNameDialog(){
        TextInputDialog dialog2 = new TextInputDialog("");
        dialog2.setTitle("core.Repository name");
        dialog2.setHeaderText("Enter repository name:");
        dialog2.setContentText("Name:");
        Optional<String> repoName = dialog2.showAndWait();
        return repoName.get();
    }

}
