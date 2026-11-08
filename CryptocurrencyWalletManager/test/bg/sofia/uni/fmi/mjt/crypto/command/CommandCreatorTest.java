package bg.sofia.uni.fmi.mjt.crypto.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommandCreatorTest {

    @Test
    void testNewCommand() {
        String[] arguments = {"Bacekalki", "parola"};
        Command expected = new Command("login", arguments);
        Command actual = CommandCreator.newCommand("\"login\" \"Bacekalki\" \"parola\"");
        assertEquals(expected.command(), actual.command());
        assertArrayEquals(expected.arguments(), actual.arguments());
    }
}
