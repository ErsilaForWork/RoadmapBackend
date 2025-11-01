package ers.roadmap.exceptions;

public class ConstraintsNotMetException extends RuntimeException {
    public ConstraintsNotMetException(String message) {
        super(message);
    }
}
