package bg.sofia.uni.fmi.mjt.crypto.datamanager;

import bg.sofia.uni.fmi.mjt.crypto.user.User;

import java.nio.file.Path;

public interface DataManagerAPI {

    void serializeData(User user);

    User getData(Path file);

    boolean exists(Path file);

}
