package net.java.hms_backend.exception;

public class HotelInfoException extends RuntimeException {

    public HotelInfoException(String message) {
        super(message);
    }

    public static class InvalidWeekendSurchargeException extends HotelInfoException {
        public InvalidWeekendSurchargeException(String message) {
            super(message);
        }
    }
}
