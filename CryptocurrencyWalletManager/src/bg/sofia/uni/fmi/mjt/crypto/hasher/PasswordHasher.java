package bg.sofia.uni.fmi.mjt.crypto.hasher;

import bg.sofia.uni.fmi.mjt.crypto.logger.ErrorLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    private static final String LOG_PATH = "logs.txt";
    private static final ErrorLogger LOGGER = new ErrorLogger(LOG_PATH);
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(password.getBytes());

            byte[] hashedBytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.logError(e.getMessage());
            throw new RuntimeException("Error hashing password.", e);
        }
    }
}
