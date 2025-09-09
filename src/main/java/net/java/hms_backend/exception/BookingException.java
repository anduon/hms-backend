package net.java.hms_backend.exception;

public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }

    public static class BookingConflictException extends BookingException {
      public BookingConflictException(String message) {
      super(message);
    }
    }


}
