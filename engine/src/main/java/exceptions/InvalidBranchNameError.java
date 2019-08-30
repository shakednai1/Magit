package exceptions;

public class InvalidBranchNameError extends Exception {
    public InvalidBranchNameError(String errorMessage) {
        super(errorMessage);
    }
}