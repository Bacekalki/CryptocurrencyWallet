package bg.sofia.uni.fmi.mjt.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class CryptoWalletClient {
    private static final int SERVER_PORT = 1234;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;
    private static final String CLIENT_LOGS = "clientLogs.txt";
    private static final ClientErrorLogger LOGGER = new ClientErrorLogger(CLIENT_LOGS);
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static void writeToServer(SocketChannel socketChannel, ByteBuffer buffer, String message)
        throws IOException {
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
    }

    private static String getServerReply(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        String reply = new String(byteArray, "UTF-8");
        return reply;
    }

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine();

                writeToServer(socketChannel, buffer, message);

                String reply = getServerReply(socketChannel, buffer);

                System.out.println("The server replied <" + reply + ">");

                if ("quit".equals(message)) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println(
                "There is a problem with the network communication. Please send the developers the logs in " +
                    CLIENT_LOGS);
            LOGGER.logError(e.toString());
        }
    }
}
