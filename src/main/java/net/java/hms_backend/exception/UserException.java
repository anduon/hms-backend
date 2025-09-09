package net.java.hms_backend.exception;

public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }

    public static class DuplicateEmailException extends UserException {
        public DuplicateEmailException(String message) {
            super(message);
        }
    }

    public static class InvalidPasswordException extends UserException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    public static class MissingPasswordException extends UserException {
        public MissingPasswordException(String message) {
            super(message);
        }
    }
}
