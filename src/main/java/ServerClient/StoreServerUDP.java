package ServerClient;

import Structure.FakeReceiver;
import Structure.Sender;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreServerUDP extends Thread {
    public static Map<Integer, Pair<InetAddress, Integer>> clientMap = new ConcurrentHashMap<>();
    private DatagramSocket socket;
    private FakeReceiver receiver;
    private boolean running;
    private final int BUFFER_SIZE = 128;
    private byte[] buffer;

    public StoreServerUDP(int port) throws IOException {
        socket = new DatagramSocket(port);

        receiver = new FakeReceiver();
        receiver.start();

        Sender.isServerTCP = false;
    }

    public void run() {
        running = true;

        while (running) {
            buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                ByteBuffer message = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());

                receiver.receiveMessage(message.array());
            } catch (SocketException e) {
                this.doStop();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            try {
                clientMap.put(getUserID(packet.getData()), new Pair<>(address, port));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        receiver.doStop();
        socket.close();
    }

    private int getUserID(byte[] message) {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        buffer.get();

        buffer.get();
        buffer.getLong();

        buffer.getInt();
        buffer.getShort();
        buffer.getInt();

        return buffer.getInt();
    }
    public void doStop() {
        running = false;
    }
}
