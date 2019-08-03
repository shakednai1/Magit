import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
                    // TODO load XML
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
        System.out.println("Current repository location: " + engine.getCurrentRepoLocation());
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
        System.out.println("12. Exit");
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
        List<String> res = engine.getCurrentCommitState();
        for(String item : res){
            System.out.println(item);
        }
        System.out.println();
    }

    private static void printWorkingCopyStatus(){
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
        List<String> allBranchesNames = engine.getAllBranches();
        allBranchesNames.forEach(System.out::println);
    }

    private static void createBranch(Scanner input){
        boolean isValidBranchName = false;
        while(!isValidBranchName){
            System.out.println("Please provide branch name: ");
            String branchName = input.next();
            if (!engine.createNewBranch(branchName)){
                System.out.println("engine.Branch name is already exist! ");
            }
            else{
                isValidBranchName=true;
            }
        }
    }

    private static void deleteBranch(Scanner input){
        while (true){
            System.out.println("Please provide branch name to delete: ");
            String branchName = input.next();
            if(engine.deleteBranch(branchName)){
                break;
            }
            else {
                System.out.println("Given branch name is the head branch. Cannot delete head branch! ");
            }
        }
    }

    private static void printBranchHistory(){
        List<String> activeBranchHistory = engine.getActiveBranchHistory();
        for (String item: activeBranchHistory){
            System.out.println(item);
        }
    }

    private static void checkoutBranch(Scanner input){
        while (true) {
            System.out.println("Please provide branch name to checkout: ");
            String branchName = input.next();
            if (engine.validBranchName(branchName)) {
                checkoutToValidBranch(input, branchName);
                break;
            } else {
                System.out.println("Given branch name is not valid. Please provide an existing branch name ");
            }
        }
    }

    private static void checkoutToValidBranch(Scanner input, String branchName){
        if(engine.haveOpenChanges()){
            System.out.println("Given Branch has open changes. you need to commit then or force checkout and the " +
                    "changes will remove ");
            System.out.println("force commit ? Y/N");
            String response = input.next();
            if(response.equals("Y")){
                engine.checkoutBranch(branchName);
            }
            else{
                checkoutBranch(input);
            }
        }
        else{
            engine.checkoutBranch(branchName);
        }
    }

}
