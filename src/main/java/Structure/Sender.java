package Structure;

import Packet.Packet;
import ServerClient.StoreServerTCP;
import Packet.Message;
import ServerClient.StoreServerUDP;
import javafx.util.Pair;


import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sender extends Thread {

    public static boolean isServerTCP;

    private boolean stop;
    private ExecutorService service;

    private static final int THREAD_AMOUNT = 10;

    private ConcurrentLinkedQueue<Pair<Integer, byte[]>> queueOfPackets;

    public void run() {
        stop = true;

        queueOfPackets = new ConcurrentLinkedQueue<>();
        service = Executors.newFixedThreadPool(THREAD_AMOUNT);

        while (stop) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (queueOfPackets.size() != 0) {
                            Pair<Integer, byte[]> pair = queueOfPackets.poll();
                            command(pair.getKey(), pair.getValue());
                        }
                    } catch (Exception e) {
                    }
                }
            });
        }
        service.shutdown();
    }

    public void doStop() {
        stop = false;
    }

    public void sendMessage(int clientId, byte[] packet) {
        queueOfPackets.add(new Pair<>(clientId, packet));
    }

    private void command(int target, byte[] message) throws Exception {
        if (isServerTCP) {
            Socket clientSocket = StoreServerTCP.clientMap.get(target);
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(parseMessageIntoString(message));

        } else {
            Pair<InetAddress, Integer> address = StoreServerUDP.clientMap.get(target);
            DatagramPacket packet = new DatagramPacket(message, message.length, address.getKey(), address.getValue());
            new DatagramSocket().send(packet);
        }
    }

    private String parseMessageIntoString(byte[] message) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(message);
        Packet packet = new Packet();

        while (buffer.remaining() != 0) {
            if (buffer.get() == Packet.getBMagic()) {

                packet.setBSrc(buffer.get());
                packet.setBPktId(buffer.getLong());

                int wLen = buffer.getInt();
                packet.setWLen(wLen);

                packet.setWCrc16(buffer.getShort());

                packet.setBMsg(new Message(buffer, wLen));

                packet.setWCrc16Msg(buffer.getShort());
            }
        }
        return packet.toString();
    }
}
