package bg.sofia.uni.fmi.mjt.crypto.datamanager;

import bg.sofia.uni.fmi.mjt.crypto.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class DataManagerTest {

    private DataManager dataManager;

    @BeforeEach
    void setUp() {
        dataManager = new DataManager();
    }

    @Test
    void testSerializeAndDeserializeUser() {
        User user = new User("testUser", "password123");

        Path file = Path.of("testUser.txt");
        dataManager.serializeData(user);

        assertTrue(Files.exists(file));

        User retrievedUser = dataManager.getData(file);
        assertNotNull(retrievedUser);
        assertEquals(user.getUsername(), retrievedUser.getUsername());
        assertEquals(user.getHashedPassword(), retrievedUser.getHashedPassword());
    }

    @Test
    void testGetDataFileNotFound() {
        Path nonExistingFile = Path.of("non/existing/file.txt");

        assertNull(dataManager.getData(nonExistingFile));
    }

}


