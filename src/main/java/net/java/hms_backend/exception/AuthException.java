package net.java.hms_backend.exception;

public class AuthException extends RuntimeException {
  public AuthException(String message) {
    super(message);
  }

  public static class MissingEmailException extends AuthException {
    public MissingEmailException() {
      super("Email is required");
    }
  }

  public static class MissingPasswordException extends AuthException {
    public MissingPasswordException() {
      super("Password is required");
    }
  }

  public static class MissingEmailAndPasswordException extends AuthException {
    public MissingEmailAndPasswordException() {
      super("Email and password are required");
    }
  }
}
