package bg.sofia.uni.fmi.mjt.crypto.exception;

public class OpeningFileException extends RuntimeException {
    public OpeningFileException(String message) {
        super(message);
    }

    public OpeningFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
