package net.java.hms_backend.exception;

public class MissingPasswordException extends RuntimeException {
    public MissingPasswordException(String message) {
        super(message);
    }
}