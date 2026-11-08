package bg.sofia.uni.fmi.mjt.crypto.datamanager;

import bg.sofia.uni.fmi.mjt.crypto.exception.OpeningFileException;
import bg.sofia.uni.fmi.mjt.crypto.logger.ErrorLogger;
import bg.sofia.uni.fmi.mjt.crypto.user.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataManager implements DataManagerAPI {
    private static final String LOG_PATH = "logs.txt";
    private static final ErrorLogger LOGGER = new ErrorLogger(LOG_PATH);
    public void serializeData(User user) {
        Path file = Path.of(user.getUsername() + ".txt");
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException e) {
                LOGGER.logError(e.getMessage());
                throw new OpeningFileException("File couldn't be created", e);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file.toFile()))) {
            oos.writeObject(user);
        } catch (IOException e) {
            LOGGER.logError(e.getMessage());
            throw new OpeningFileException("File couldn't be opened for writing", e);
        }
    }

    public User getData(Path file) {
        if (Files.exists(file)) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toFile()))) {
                return (User) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.logError(e.getMessage());
                throw new OpeningFileException("File couldn't be opened for reading", e);
            }
        }

        return null;
    }

    public boolean exists(Path file) {
        return Files.exists(file);
    }
}
