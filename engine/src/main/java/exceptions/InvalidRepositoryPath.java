package exceptions;

public class InvalidRepositoryPath extends Exception {
    public InvalidRepositoryPath(String errorMessage) {
        super(errorMessage);
    }

}
