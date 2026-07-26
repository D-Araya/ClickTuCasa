package domain.exception;

public class InvalidRaffleOperationException extends RuntimeException {
    public InvalidRaffleOperationException(String message) {
        super(message);
    }
}
