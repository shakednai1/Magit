package exceptions;

public class UncommittedChangesError extends Exception {
    public UncommittedChangesError(String errorMessage) {
        super(errorMessage);
    }
}