package bootcamp.hibernate_practical.exception;

public class BookStateConflictException extends RuntimeException {
    public BookStateConflictException(String message) {
        super(message);
    }
}
