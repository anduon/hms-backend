package net.java.hms_backend.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RoomException.DuplicateRoomException.class)
    public ResponseEntity<String> handleDuplicateRoom(RoomException.DuplicateRoomException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserException.DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmail(UserException.DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(BookingException.BookingConflictException.class)
    public ResponseEntity<String> handleBookingConflict(BookingException.BookingConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvoiceException.PdfGenerationException.class)
    public ResponseEntity<String> handlePdfGeneration(InvoiceException.PdfGenerationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(UserException.MissingPasswordException.class)
    public ResponseEntity<String> handleMissingPassword(UserException.MissingPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UserException.InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(UserException.InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RoomException.NullRoomNumberException.class)
    public ResponseEntity<String> handleNullRoomNumber(RoomException.NullRoomNumberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AssetException.NullRoomNumberException.class)
    public ResponseEntity<String> handleNullRoomNumberAsset(AssetException.NullRoomNumberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler({
            AuthException.MissingEmailException.class,
            AuthException.MissingPasswordException.class,
            AuthException.MissingEmailAndPasswordException.class
    })
    public ResponseEntity<Map<String, String>> handleMissingAuthFields(AuthException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UserException.AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(UserException.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
