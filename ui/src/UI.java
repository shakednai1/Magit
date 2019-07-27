import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class UI {

    public static void main(String args[]){
        Scanner input = new Scanner(System.in);
        MainEngine engine = new MainEngine();
        boolean toContinue = true;

        while(toContinue){
            System.out.println();
            System.out.println("Magit Menu");
            System.out.println("Current logged in user: " + MainEngine.currentUser);
            System.out.println("Current repository location: " + MainEngine.getCurrentRepoLocation());
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

            // TODO change to get next() and validate the input is illegal digit and print error accordingly
            //  (number is not in menu / unsupported type like char)
            int number = input.nextInt();
            switch (number){
                case 1:
                    System.out.println("Please enter username: ");
                    String user = input.next();
                    MainEngine.chnageCurrentUser(user);
                    break;
                case 3:
                    boolean isValid = false;
                    while(!isValid){
                        System.out.println("Please provide repository full path: ");
                        String fullPath = input.next();
                        if (!MainEngine.changeActiveRepository(fullPath)){
                            System.out.println("Repository path is not found or path is not contains .magit folder!");
                        }
                        else {
                            isValid = true;
                        }
                    }
                    break;
                case 4:
                    List<String> res = MainEngine.getCurrentCommitState();
                    for(String item : res){
                        System.out.println(item);
                    }
                    System.out.println();
                    break;
                case 5:
                    Map<String, List<String>> changes = MainEngine.getWorkingCopyStatus();
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
                    break;
                case 6:
                    System.out.println("Please enter commit message: ");
                    // TODO not working with spaces - find the solution
                    String msg = input.next();
                    input.nextLine();
                    if (!MainEngine.commit(msg)){
                        System.out.println("There are no changes to commit");
                        System.out.println();
                    }
                    break;
                case 7:
                    List<String> allBranchesNames = MainEngine.getAllBranches();
                    for(String branch: allBranchesNames){
                        System.out.println(branch);
                    }
                    break;
                case 8:
                    boolean isValidBranchName = false;
                    while(!isValidBranchName){
                        System.out.println("Please provide branch name: ");
                        String branchName = input.next();
                        if (!MainEngine.createNewBranch(branchName)){
                            System.out.println("Branch name is already exist! ");
                        }
                        else{
                            isValidBranchName=true;
                        }
                    }
                    break;
                case 9:
                    boolean isValidBranchToDelete = false;
                    while (!isValidBranchToDelete){
                        System.out.println("Please provide branch name to delete: ");
                        String branchName = input.next();
                        if(!MainEngine.deleteBranch(branchName)){
                            System.out.println("Given branch name is the head branch. Cannot delete head branch! ");
                        }
                        else {
                            isValidBranchToDelete = true;
                        }
                    }
                    break;
                case 11:
                    List<String> activeBranchHistory = MainEngine.getActiveBrancHistory();
                    for (String item: activeBranchHistory){
                        System.out.println(item);
                    }
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

}
