package net.java.hms_backend.exception;

public class RoomException extends RuntimeException {
    public RoomException(String message) {
        super(message);
    }

  public static class DuplicateRoomException extends RoomException {
    public DuplicateRoomException(String message) {
      super(message);
    }
  }

  public static class NullRoomNumberException extends RoomException {
        public NullRoomNumberException(String message) { super(message);}
  }
}
