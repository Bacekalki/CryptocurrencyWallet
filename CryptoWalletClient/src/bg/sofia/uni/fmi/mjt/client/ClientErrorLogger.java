package bg.sofia.uni.fmi.mjt.client;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientErrorLogger {

    private final String logFilePath;

    public ClientErrorLogger(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void logError(String errorMessage) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.println(timestamp + " - ERROR: " + errorMessage);
        } catch (IOException e) {
            throw new RuntimeException("There was an error with the logger", e);
        }
    }
}
