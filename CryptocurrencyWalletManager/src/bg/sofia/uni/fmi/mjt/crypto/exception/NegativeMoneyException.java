package bg.sofia.uni.fmi.mjt.crypto.exception;

public class NegativeMoneyException extends Exception {
    public NegativeMoneyException(String message) {
        super(message);
    }

    public NegativeMoneyException(String message, Throwable cause) {
        super(message, cause);
    }
}
