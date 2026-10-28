package bootcamp.hibernate_practical.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BookExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Void> handleBookNotFound(BookNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(BookStateConflictException.class)
    public ResponseEntity<Void> handleBookStateConflict(BookStateConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
