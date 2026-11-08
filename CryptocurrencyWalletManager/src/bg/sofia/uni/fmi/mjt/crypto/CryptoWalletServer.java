package bg.sofia.uni.fmi.mjt.crypto;

import bg.sofia.uni.fmi.mjt.crypto.command.Command;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.crypto.command.CommandExecutorAPI;
import bg.sofia.uni.fmi.mjt.crypto.logger.ErrorLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CryptoWalletServer {
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";
    private static final int PORT = 1234;
    private static final int TIME_UNITS_BETWEEN_REQUESTS = 30;
    private static final String LOG_PATH = "logs.txt";
    private final ErrorLogger errorLogger = new ErrorLogger(LOG_PATH);
    private boolean isServerWorking;
    private ByteBuffer buffer;
    private Selector selector;
    private final CommandExecutorAPI executor;

    public CryptoWalletServer() {
        executor = new CommandExecutor();
    }

    public void start() {
        var threadPool = new ScheduledThreadPoolExecutor(1);
        threadPool.scheduleAtFixedRate(executor::updatePrices, 0, TIME_UNITS_BETWEEN_REQUESTS,
            TimeUnit.MINUTES);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            setUp(serverSocketChannel);

            while (isServerWorking) {
                if (selector.select() == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                iterateKeys(keyIterator);
            }
        } catch (IOException e) {
            threadPool.shutdown();
            errorLogger.logError(e.toString());
            throw new UncheckedIOException("There was an error with the server", e);
        }
    }

    private void setUp(ServerSocketChannel serverSocketChannel) {
        try {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
        } catch (IOException e) {
            errorLogger.logError(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean loginCheck(SelectionKey key, Command cmd, SocketChannel clientChannel) throws IOException {
        if (key.attachment() != null) {
            if (cmd.command().equals("login") || cmd.command().equals("register")) {
                String output = "You have to logout first";
                writeClientOutput(clientChannel, output);
                return true;
            }
        }
        return false;
    }

    private void successfulLoginOrRegister(SelectionKey key, String output, Command cmd) {
        if ((cmd.command().equals("login") && output.equals("Logged in successfully"))
            || (cmd.command().equals("register") && output.equals("Registration successful"))) {

            key.attach(cmd.arguments()[0]);
        }
    }

    private void logoutOrQuit(SelectionKey key, String output, Command cmd, SocketChannel clientChannel)
        throws IOException {
        if (cmd.command().equals("logout") && output.equals("Logged out successfully")) {
            key.attach(null);
        }

        if (cmd.command().equals("quit")) {
            key.cancel();
            clientChannel.close();
        }
    }

    private void iterateKeys(Iterator<SelectionKey> keyIterator) throws IOException {
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                try {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    String clientInput = getClientInput(clientChannel);
                    String output;
                    if (clientInput == null) {
                        continue;
                    }
                    Command cmd = CommandCreator.newCommand(clientInput);
                    if (loginCheck(key, cmd, clientChannel)) {
                        keyIterator.remove();
                        continue;
                    }
                    output = executor.execute((String) key.attachment(), cmd);
                    successfulLoginOrRegister(key, output, cmd);
                    writeClientOutput(clientChannel, output);
                    logoutOrQuit(key, output, cmd, clientChannel);
                } catch (IOException e) {
                    errorLogger.logError(e.toString());
                    continue;
                }
            } else if (key.isAcceptable()) {
                accept(selector, key);
            }
            keyIterator.remove();
        }
    }


    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();


        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        CryptoWalletServer server = new CryptoWalletServer();
        server.start();
    }
}
