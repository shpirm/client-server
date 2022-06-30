package Structure;

import Packet.Packet;
import javafx.util.Pair;

import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class FakeReceiver extends Thread implements Receiver {

    private static final int THREAD_AMOUNT = 10;
    private boolean stop;
    private ExecutorService service;

    private Descriptor descriptor;



    private ConcurrentLinkedQueue<byte[]> queueOfPackets;

    public void run() {
        stop = true;

        queueOfPackets = new ConcurrentLinkedQueue<>();
        service = Executors.newFixedThreadPool(THREAD_AMOUNT);
        descriptor = new Descriptor();
        descriptor.start();

        while (stop) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (queueOfPackets.size() != 0)
                            descriptor.decrypt(queueOfPackets.poll());

                    } catch (Exception e) {
                    }
                }
            });
        }
        service.shutdown();
    }

    public void doStop() {
        stop = false;
        descriptor.doStop();
    }

    @Override
    public void receiveMessage(byte[] packet) {
        queueOfPackets.add(packet);
    }

    private byte[] getBrokenPacket() {
        final int PACKET_MAX_SIZE = 20;

        Random rn = new Random();
        final int packetSize = rn.nextInt(PACKET_MAX_SIZE) + 1;

        byte[] brokenPacketArray = new byte[packetSize];
        brokenPacketArray[rn.nextInt(packetSize)] = Packet.getBMagic();

        return brokenPacketArray;
    }

}
