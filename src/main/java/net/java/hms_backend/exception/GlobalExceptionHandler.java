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

    @ExceptionHandler({
            InvoiceException.PdfGenerationException.class,
            InvoiceException.DuplicateBookingException.class
    })
    public ResponseEntity<Map<String, String>> handleInvoiceExceptions(InvoiceException ex) {
        HttpStatus status;

        if (ex instanceof InvoiceException.PdfGenerationException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (ex instanceof InvoiceException.DuplicateBookingException) {
            status = HttpStatus.CONFLICT;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status)
                .body(Map.of("message", ex.getMessage()));
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

    @ExceptionHandler({
            UserException.DuplicateEmailException.class,
            UserException.MissingPasswordException.class,
            UserException.InvalidPasswordException.class,
            UserException.AccessDeniedException.class
    })
    public ResponseEntity<?> handleUserExceptions(UserException ex) {
        HttpStatus status;

        if (ex instanceof UserException.DuplicateEmailException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof UserException.InvalidPasswordException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof UserException.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity.status(status)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            BookingException.BookingConflictException.class,
            BookingException.MissingGuestNameException.class,
            BookingException.MissingIdNumberException.class,
            BookingException.MissingRoomNumberException.class,
            BookingException.MissingCheckInDateException.class,
            BookingException.MissingCheckOutDateException.class,
            BookingException.MissingBookingTypeException.class,
            BookingException.MissingStatusException.class,
            BookingException.MissingNumberOfGuestsException.class,
            BookingException.InvalidDateRangeException.class,
            BookingException.InvalidBookingTypeException.class
    })
    public ResponseEntity<Map<String, String>> handleBookingExceptions(BookingException ex) {
        HttpStatus status = ex instanceof BookingException.BookingConflictException
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            RoomException.DuplicateRoomException.class,
            RoomException.NullRoomNumberException.class
    })
    public ResponseEntity<Map<String, String>>handleRoomExceptions(RoomException ex) {
        HttpStatus status = ex instanceof RoomException.DuplicateRoomException
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            AssetException.NullRoomNumberException.class
    })
    public ResponseEntity<Map<String, String>>handleAssetExceptions(AssetException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({
            PromotionException.MissingNameException.class,
            PromotionException.MissingDiscountPercentException.class,
            PromotionException.MissingStartDateException.class,
            PromotionException.MissingEndDateException.class,
            PromotionException.InvalidDateRangeException.class,
            PromotionException.InvalidDiscountRangeException.class
    })
    public ResponseEntity<Map<String, String>> handlePromotionExceptions(PromotionException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

}
