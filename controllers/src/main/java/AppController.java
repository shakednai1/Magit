import commitTree.CommitTree;
import commitTree.node.CommitNode;
import commitTree.node.CommitNodeController;
import core.*;
import exceptions.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import models.BranchData;
import models.CommitData;
import models.RepositoryModel;
import workingCopy.WorkingCopyStage;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class AppController {

    private MainEngine engine ;
    private RepositoryModel repositoryModel;
    private CommitTree commitTree;
    private Map<String, CommitData> commits = new HashMap<>(); // TODO make observable & commit tree should listen
    private List<BranchData> branches = new LinkedList<>();

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
        TextInputDialog dialog = new TextInputDialog("");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        dialog.setTitle("Create new branch");
        dialog.setHeaderText("Enter branch name");
        dialog.setContentText("Name");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).visibleProperty().set(false);

        Validator validBranch = (value) -> {
            try {
                createNewBranch(value);
                return true;
            }
            catch (InvalidBranchNameError | UncommittedChangesError | NoActiveRepositoryError e) {
                showErrorAlert(e);
            }
            return false;
        };

        dialog.show();

        final Button btOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(
                ActionEvent.ACTION,
                OKevent -> {
                    if (!validBranch.isValid(dialog.getEditor().textProperty().getValue())) {
                        OKevent.consume();
                    }
                }
        );
    }

    private void createNewBranch(String branchName) throws InvalidBranchNameError, UncommittedChangesError, NoActiveRepositoryError{
        BranchData branchData = engine.createNewBranch(branchName, false);
        branchData.getHeadSha1Property().addListener(new BranchHeadCommitChangedListener(branchData));
        branches.add(branchData);
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
            engine.loadRepositoyFromXML();
            updateCurrentRepo(engine.getCurrentRepoPath());
        }
        catch (XmlException | UncommittedChangesError | InvalidBranchNameError e){
            showErrorAlert(e);
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
                CommitData commitData = engine.commit(commitMsg.get());
                commitTree.addCommit(commitData);
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

    private List<BranchData> getAllBranchesDetails() throws NoActiveRepositoryError{
        return engine.getAllBranches();
    }

    private void updateCurrentRepo(String repoPath){
        try{
            repositoryModel.setRepo(engine.getCurrentRepoName(), repoPath);
            updateBranch();

            branches.clear();
            branches = engine.getAllBranches();

            commits.clear();
            commits = engine.getAllCommitsData();
            commitTree.setCommitsTree(engine.getAllCommitsData());
        }
        catch (NoActiveRepositoryError e){

        }
    }

    private void updateBranch(){
        currentBranch.setText("Current core.Branch: " + engine.getCurrentBranchName());
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
        List<String> branchesName = null;
        try {
            branchesName = getAllBranchNames();
            branchesName.remove(engine.getCurrentBranchName());
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<String>(branchesName.get(0), branchesName);
            choiceDialog.setContentText("Choose branch to Merge with the head branch");
            choiceDialog.setHeaderText("Merge branch");
            Optional<String> branchToMerge = choiceDialog.showAndWait();
            List<FileChanges> conflicts = engine.merge(branchToMerge.get());
            if(conflicts != null){
                // TODO show conflicts
            }
        } catch (NoActiveRepositoryError e) {
            showErrorAlert(e);
        }

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
