package exceptions;

public class NoActiveRepositoryError extends Exception{
    public NoActiveRepositoryError(String errorMessage) {
        super(errorMessage);
    }

}
