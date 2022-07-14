package Structure;

import Packet.Packet;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Encryptor extends Thread {

    private boolean stop;
    private ExecutorService service;
    private Sender sender;

    private static final int THREAD_AMOUNT = 10;


    private ConcurrentLinkedQueue<Packet> queueOfPackets;

    public void run() {
        stop = true;

        queueOfPackets = new ConcurrentLinkedQueue<>();
        service = Executors.newFixedThreadPool(THREAD_AMOUNT);

        sender = new Sender();

        sender.start();

        while (stop) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (queueOfPackets.size() != 0)
                            command(queueOfPackets.poll());
                    } catch (Exception e) {
                    }
                }
            });
        }
        service.shutdown();
    }

    public void doStop() {
        stop = false;
        sender.doStop();
    }

    public void encrypt(Packet packet) {
        queueOfPackets.add(packet);
    }

    private void command(Packet packet) throws Exception {

        final int COMMAND_SIZE = packet.getBMsg().getMessage().length;
        final int MESSAGE_SIZE = Integer.BYTES + Integer.BYTES + COMMAND_SIZE;
        final int PACKET_DATA_SIZE = Byte.BYTES + Byte.BYTES
                + Long.BYTES + Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_DATA_SIZE + Short.BYTES
                + MESSAGE_SIZE + Short.BYTES);

        buffer.put(Packet.getBMagic());
        buffer.put(packet.getBSrc());

        buffer.putLong(packet.getBPktId());
        buffer.putInt(packet.getWLen());

        buffer.putShort(packet.getWCrc16());

        buffer.putInt(packet.getBMsg().getCType());
        buffer.putInt(packet.getBMsg().getBUserId());
        buffer.put(packet.getBMsg().getMessage());

        buffer.putShort(packet.getWCrc16Msg());

        sender.sendMessage(packet.getBMsg().getBUserId(), buffer.array());
    }
}
