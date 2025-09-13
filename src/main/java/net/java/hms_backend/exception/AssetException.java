package net.java.hms_backend.exception;

public class AssetException extends RuntimeException {
    public AssetException(String message) {
        super(message);
    }

    public static class NullRoomNumberException extends AssetException {
        public NullRoomNumberException(String message) {
            super(message);
        }
    }
}
