package net.java.hms_backend.exception;

public class PromotionException extends RuntimeException {
    public PromotionException(String message) {
        super(message);
    }

    public static class MissingNameException extends PromotionException {
        public MissingNameException() {
            super("Promotion name is required");
        }
    }

    public static class MissingDiscountPercentException extends PromotionException {
        public MissingDiscountPercentException() {
            super("Discount percent is required");
        }
    }

    public static class MissingStartDateException extends PromotionException {
        public MissingStartDateException() {
            super("Start date is required");
        }
    }

    public static class MissingEndDateException extends PromotionException {
        public MissingEndDateException() {
            super("End date is required");
        }
    }

    public static class InvalidDateRangeException extends PromotionException {
        public InvalidDateRangeException() {
            super("End date must be after start date");
        }
    }

    public static class InvalidDiscountRangeException extends PromotionException {
        public InvalidDiscountRangeException() {
            super("Discount percent must be between 0 and 100");
        }
    }
}
