package bg.sofia.uni.fmi.mjt.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        File file = new File("test.sdocx");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.lines().forEach(line -> System.out.println(Arrays.toString(line.getBytes(StandardCharsets.UTF_8))));
    }

}
