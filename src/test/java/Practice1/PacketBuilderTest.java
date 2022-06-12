package Practice1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class PacketBuilderTest {
    @Test
    void packetBuilderTest() throws Exception {
        PacketBuilder packetBuilder = new PacketBuilder();

        ByteBuffer buffer = ByteBuffer.allocate(0);

        final int PACKET_AMOUNT = 4;
        for (int i = 0; i < PACKET_AMOUNT; i++) {
            byte[] packet = getRandomPacket();

            ByteBuffer currentBuffer = ByteBuffer.allocate(
                    buffer.capacity() + packet.length);
            currentBuffer.put(buffer.array()).put(packet);
            buffer = currentBuffer;
        }

        ArrayList<Packet> packets = packetBuilder.decodePacket(buffer.array());

        for (Packet packet : packets) System.out.println(packet);

        byte[] encodedPacketArray = packetBuilder.encodePacket(packets);
        Assertions.assertArrayEquals(buffer.array(),encodedPacketArray);

        byte[] brokenPacket = getBrokenPacket();
        ByteBuffer brokenBuffer = ByteBuffer.allocate(
                buffer.capacity() + brokenPacket.length);

        brokenBuffer.put(brokenPacket);

        int brokenPacketAmount = 0;
        try {
            packets = packetBuilder.decodePacket(brokenBuffer.array());
        } catch (Exception e) {
            ++brokenPacketAmount; }
        System.out.println("Broken Packets found: " + brokenPacketAmount);

        byte[] encodedBrokenPacketArray = packetBuilder.encodePacket(packets);
        Assertions.assertArrayEquals(buffer.array(),encodedBrokenPacketArray);
    }

    private byte[] getBrokenPacket() {
        final int PACKET_MAX_SIZE = 20;

        Random rn = new Random();
        final int packetSize = rn.nextInt(PACKET_MAX_SIZE) + 1;

        byte[] brokenPacketArray = new byte[packetSize];
        brokenPacketArray[rn.nextInt(packetSize)] = Packet.getBMagic();

        return brokenPacketArray;
    }
    private byte[] getRandomPacket() {

        final int COMMAND_SIZE = 4;
        final int MESSAGE_SIZE = Integer.BYTES + Integer.BYTES + COMMAND_SIZE;
        final int PACKET_DATA_SIZE = Byte.BYTES + Byte.BYTES
                + Long.BYTES + Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_DATA_SIZE + Short.BYTES
                + MESSAGE_SIZE + Short.BYTES);

        Random rn = new Random();
        ByteBuffer bufferPkt = ByteBuffer.allocate(PACKET_DATA_SIZE);

        bufferPkt.put(Packet.getBMagic())
                .put((byte) rn.nextInt())
                .putLong(rn.nextLong())
                .putInt(MESSAGE_SIZE);

        short wCrc16Pkt = CRC16.getCRC16(bufferPkt.array());
        buffer.put(bufferPkt.array())
                .putShort(wCrc16Pkt);

        ByteBuffer bufferMsg = ByteBuffer.allocate(MESSAGE_SIZE);
        bufferMsg.putInt(rn.nextInt())
                .putInt(rn.nextInt())
                .putInt(rn.nextInt());

        short wCrc16Msg = CRC16.getCRC16(bufferMsg.array());
        buffer.put(bufferMsg.array())
                .putShort(wCrc16Msg);

        return buffer.array();
    }
}
