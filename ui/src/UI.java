import exceptions.InvalidBranchNameError;
import exceptions.NoActiveRepositoryError;
import exceptions.UncommittedChangesError;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

public class UI {

    private static MainEngine engine ;

    public static void main(String args[]){
        Scanner input = new Scanner(System.in);
        engine = new MainEngine();
        boolean toContinue = true;

        while(toContinue){
            printMenu();
            validateMenu();

            int number = input.nextInt();
            switch (number){
                case 1:
                    getSetUser(input);
                    break;
                case 2:
                    loadFromXml(input);
                    break;
                case 3:
                    switchRepository(input);
                    break;
                case 4:
                    printCommitState();
                    break;
                case 5:
                    printWorkingCopyStatus();
                    break;
                case 6:
                    commit(input);
                    break;
                case 7:
                    printBranches();
                    break;
                case 8:
                    createBranch(input);
                    break;
                case 9:
                    deleteBranch(input);
                    break;
                case 10:
                    checkoutBranch(input);
                    break;
                case 11:
                    printBranchHistory();
                    break;
                case 12:
                    createNewRepository(input);
                    break;
                case 13:
                    toContinue = false;
                    System.out.println("Bye Bye !");
                    System.out.println();
                    break;
                default:
                    System.out.println("nothing");
            }
        }
    }

    private static void printMenu(){
        System.out.println();
        System.out.println("Magit Menu");
        System.out.println("Current logged in user: " + engine.getUser());
        System.out.println("Current repository location: " + getCurrentRepoName());
        System.out.println("1. Change user name");
        System.out.println("2. Load from XML");
        System.out.println("3. Switch repository");
        System.out.println("4. Show current commit file system information");
        System.out.println("5. Working copy status");
        System.out.println("6. Commit");
        System.out.println("7. List available branch");
        System.out.println("8. Create new branch");
        System.out.println("9. Delete branch");
        System.out.println("10. Checkout branch");
        System.out.println("11. Show current branch history");
        System.out.println("12. Create new repository");

        System.out.println("13. Exit");
    }

    private static void validateMenu(){
        // TODO change to get next() and validate the input is illegal digit and print error accordingly
        //  (number is not in menu / unsupported type like char)
    }

    private static void getSetUser(Scanner input){
        System.out.println("Please enter username: ");
        String user = input.next();
        engine.changeCurrentUser(user);
    }

    private static void switchRepository(Scanner input){
        boolean isValid = false;
        while(!isValid){
            System.out.println("Please provide repository full path: ");
            String fullPath = input.next();
            if (!engine.changeActiveRepository(fullPath)){
                System.out.println("engine.Repository path is not found or path is not contains .magit folder!");
            }
            else {
                isValid = true;
            }
        }
    }

    private static void printCommitState(){
        try{
            List<String> res = engine.getCurrentCommitState();
            for(String item : res){
                System.out.println(item);
            }
            System.out.println();
        }
        catch (NoActiveRepositoryError  | UncommittedChangesError e){
            System.out.println(e.getMessage());
        }
    }

    private static void printWorkingCopyStatus(){
        try{
            Map<String, List<String>> changes = engine.getWorkingCopyStatus();
            List<String> updatedFiles = changes.get("update");
            List<String> newFiles = changes.get("new");
            List<String> deletedFiles = changes.get("delete");

            System.out.println();
            System.out.println("============================");
            System.out.println("Updated Files: ");
            System.out.println("============================");
            for(String updatedFile : updatedFiles){
                System.out.println(updatedFile);
            }

            System.out.println();
            System.out.println("============================");
            System.out.println("New Files: ");
            System.out.println("============================");
            for(String newFile : newFiles){
                System.out.println(newFile);
            }

            System.out.println();
            System.out.println("============================");
            System.out.println("Deleted Files: ");
            System.out.println("============================");
            for(String deletedFile : deletedFiles){
                System.out.println(deletedFile);
            }
            System.out.println();
            System.out.println();
        }
        catch (NoActiveRepositoryError e){
            System.out.println(e.getMessage());
        }

    }

    private static void commit(Scanner input){
        System.out.println("Please enter commit message: ");
        // TODO not working with spaces - find the solution
        String msg = input.next();
        input.nextLine();
        if (!engine.commit(msg)){
            System.out.println("There are no changes to commit");
            System.out.println();
        }
    }

    private static void printBranches(){
        try {
            List<String> allBranchesNames = engine.getAllBranches();
            allBranchesNames.forEach(System.out::println);
        }
        catch (NoActiveRepositoryError e){
            System.out.println(e.getMessage());
        }
    }

    private static void createBranch(Scanner input){
        while(true){
            System.out.println("Please provide branch name: ");
            String branchName = input.next();

            System.out.println("Would you like to checkout to the new branch ? [Y/n]");
            String checkout = input.next();

            try{
                engine.createNewBranch(branchName, checkout.equalsIgnoreCase("Y"));
                break;
            }
            catch (InvalidBranchNameError e){
                System.out.println(branchName + " branch name is already exist! ");
            }
            catch (UncommittedChangesError e){
                System.out.println(" There are open changes. Did not checkout to "+ branchName);
            }
            catch (NoActiveRepositoryError e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private static void deleteBranch(Scanner input){
        while (true){
            System.out.println("Please provide branch name to delete: ");
            String branchName = input.next();
            try{
                engine.deleteBranch(branchName);
                break;
            }
            catch (InvalidBranchNameError e){
                System.out.println("Given branch name doesn't exist. Please provide existing branch ");
            }
            catch (IllegalArgumentException e){
                System.out.println("Given branch name is the head branch. Cannot delete head branch! ");
            }
            catch (NoActiveRepositoryError e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private static void printBranchHistory(){
        try{
            List<String> activeBranchHistory = engine.getActiveBranchHistory();
            for (String item: activeBranchHistory){
                System.out.println(item);
            }
        }
        catch (NoActiveRepositoryError e){
            System.out.println(e.getMessage());
        }
    }

    private static void checkoutBranch(Scanner input){
        boolean force = false;
        String branchName = "";

        while (true) {
            if(!force){
                System.out.println("Please provide branch name to checkout: ");
                branchName = input.next();
            }

            try {
                engine.checkoutBranch(branchName, force);
                break;
            } catch (InvalidBranchNameError e) {
                System.out.println("Given branch name is not valid. Please provide an existing branch name ");
            } catch (UncommittedChangesError e) {
                System.out.println("Given Branch has open changes. you need to commit then or force checkout and the " +
                        "changes will remove ");
                System.out.println("force commit ? Y/N");
                String forceInput = input.next();
                if (forceInput.equals("Y")) force = true;
            }
            catch (NoActiveRepositoryError e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    private static void loadFromXml(Scanner input){
        System.out.println("Please provide XML file");
        String response = input.next();
        String validationMsg = engine.isXmlValid(response);
        if(validationMsg != null){
            System.out.println(validationMsg);
        }
    }

    private static void createNewRepository(Scanner input){
        System.out.println("Please provide full path of the new repository");
        String repoPath = input.next();

        System.out.println("Please provide new repository name");
        String repoName = input.next();

        engine.createNewRepository(repoPath, repoName);
    }

    private static String getCurrentRepoName(){
        String repoName = null;
        try {
            repoName = engine.getCurrentRepoName();
        }
        catch (NoActiveRepositoryError e){
            return "";
        }
        return repoName;
    }

}
