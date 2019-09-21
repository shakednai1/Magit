package exceptions;

public class NoChangesToCommitError extends Exception {
    public NoChangesToCommitError() {
        super("There are no changes to commit");
    }
}