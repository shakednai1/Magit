package exceptions;

public class NoActiveBranchError extends Exception {
    public NoActiveBranchError(String errorMessage) {
        super(errorMessage);
    }

}
