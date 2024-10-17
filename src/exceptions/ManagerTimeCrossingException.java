package exceptions;

public class ManagerTimeCrossingException extends RuntimeException {
    public ManagerTimeCrossingException(String message) {
        super(message);
    }
}
