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

    public static class MissingGuestNameException extends BookingException {
        public MissingGuestNameException() {
            super("Guest name is required");
        }
    }

    public static class MissingIdNumberException extends BookingException {
        public MissingIdNumberException() {
            super("ID number is required");
        }
    }

    public static class MissingRoomNumberException extends BookingException {
        public MissingRoomNumberException() {
            super("Room number is required");
        }
    }

    public static class MissingCheckInDateException extends BookingException {
        public MissingCheckInDateException() {
            super("Check-in date is required");
        }
    }

    public static class MissingCheckOutDateException extends BookingException {
        public MissingCheckOutDateException() {
            super("Check-out date is required");
        }
    }

    public static class MissingBookingTypeException extends BookingException {
        public MissingBookingTypeException() {
            super("Booking type is required");
        }
    }

    public static class MissingStatusException extends BookingException {
        public MissingStatusException() {
            super("Status is required");
        }
    }

    public static class MissingNumberOfGuestsException extends BookingException {
        public MissingNumberOfGuestsException() {
            super("Number of guests is required");
        }
    }

    public static class InvalidDateRangeException extends BookingException {
        public InvalidDateRangeException() {
            super("Check-out date must be after check-in date.");
        }
    }

    public static class InvalidBookingTypeException extends BookingException {
        public InvalidBookingTypeException(String message) {
            super("Invalid booking type");
        }
    }

}
