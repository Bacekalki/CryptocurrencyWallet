package bg.sofia.uni.fmi.mjt.crypto.exception;

public class MissingCryptoException extends Exception {
    public MissingCryptoException(String message) {
        super(message);
    }

    public MissingCryptoException(String message, Throwable cause) {
        super(message, cause);
    }
}
