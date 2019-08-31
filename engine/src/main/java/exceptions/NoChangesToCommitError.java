package exceptions;

public class NoChangesToCommitError extends Exception {
    public NoChangesToCommitError(String errorMessage) {
        super(errorMessage);
    }
}