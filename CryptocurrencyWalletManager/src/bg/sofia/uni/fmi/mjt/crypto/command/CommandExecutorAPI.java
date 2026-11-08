package bg.sofia.uni.fmi.mjt.crypto.command;

public interface CommandExecutorAPI {
    String execute(String attachment, Command cmd);

    void updatePrices();
}
