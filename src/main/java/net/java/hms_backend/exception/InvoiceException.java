package net.java.hms_backend.exception;

public class InvoiceException extends RuntimeException {
    public InvoiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class PdfGenerationException extends InvoiceException {
        public PdfGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class DuplicateBookingException extends InvoiceException {
        public DuplicateBookingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
