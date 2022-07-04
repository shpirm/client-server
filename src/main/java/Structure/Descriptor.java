package Structure;

import Packet.Message;
import Packet.Packet;
import javafx.util.Pair;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Descriptor extends Thread {
    private boolean stop;
    private ExecutorService service;
    private Processor processor;

    private static final int THREAD_AMOUNT = 10;


    private ConcurrentLinkedQueue<byte[]> queueOfPackets;

    public void run() {
        stop = true;

        queueOfPackets = new ConcurrentLinkedQueue<>();
        service = Executors.newFixedThreadPool(THREAD_AMOUNT);
        processor = new Processor();
        processor.start();

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
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void doStop() {
        stop = false;
        processor.doStop();
    }

    public void decrypt(byte[] packet) {
        queueOfPackets.add(packet);
    }

    private void command(byte[] bytes) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        while (buffer.remaining() != 0) {
            if (buffer.get() == Packet.getBMagic()) {
                Packet packet = new Packet();

                packet.setBSrc(buffer.get());
                packet.setBPktId(buffer.getLong());

                int wLen = buffer.getInt();
                packet.setWLen(wLen);

                short pktWCrc16 = packet.getCrc16Pkt();
                packet.setWCrc16(buffer.getShort());

                if (pktWCrc16 == packet.getWCrc16()) {
                    packet.setBMsg(new Message(buffer, wLen));

                    packet.setWCrc16Msg(buffer.getShort());
                    short pktWCrc16Msg = packet.getBMsg().getCrc16Msg();

                    if (pktWCrc16Msg == packet.getWCrc16Msg()) {
                        processor.process(packet);
                    }
                }
            }
        }
    }
}


