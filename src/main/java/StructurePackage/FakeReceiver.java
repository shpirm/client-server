package StructurePackage;

import PacketPackage.CRC16;
import PacketPackage.Packet;
import ShopPackage.Command;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.*;

public class FakeReceiver implements Receiver {

    public static final int PRODUCT_NUMBER = 5000;
    public static final int PRODUCT_AMOUNT = 100;
    public static final int PRODUCT_PRICE = 1000;

    private static final int THREAD_AMOUNT = 20;
    private static final int PRODUCT_AMOUNT_CHANGE = 10;
    private static final int COMMAND_ID = 4;
    static ConcurrentLinkedQueue<byte[]> queueOfPackets = new ConcurrentLinkedQueue<>();

    @Override
    public void receiveMessage() throws Exception {

        int packetNumber = 0;
        JSONObject obj = new JSONObject();

        for (int i = 0; i < PRODUCT_NUMBER; i++) {
            obj.put("productName", "product" + i);
            obj.put("amount", PRODUCT_AMOUNT);
            FakeReceiver.queueOfPackets.add(getPacket(Command.PRODUCT_ADD, obj, packetNumber++));
        }

        for (int i = 0; i < PRODUCT_NUMBER; i++) {
            for (int j = 0; j < COMMAND_ID; j++) {
                Command command = Command.values()[j + 1];

                switch (command) {
                    case PRODUCT_INFO, PRODUCT_DELETE -> {
                        obj.put("productName", "product" + i);
                    }
                    case PRODUCT_INCREASE, PRODUCT_DECREASE -> {
                        obj.put("productName", "product" + i);
                        obj.put("amount", PRODUCT_AMOUNT_CHANGE);
                    }
                    case PRODUCT_PRICE -> {
                        obj.put("productName", "product" + i);
                        obj.put("price", PRODUCT_PRICE);
                    }
                }
                try {
                    FakeReceiver.queueOfPackets.add(getPacket(command, obj, packetNumber++));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (int i = 0; i < PRODUCT_NUMBER; i++) {
            Command command = Command.GROUP_CREATE;
            obj.put("groupName", "group" + i);

//            ArrayList<String> list = new ArrayList<String>();
//            for (int j = 0; j < GROUP_SIZE; j++)
//                list.add("product" + (i + j));
//            obj.put("productNames", new JSONArray(list));

            obj.put("productNames", new JSONArray(
                    new String[]{"product" + i}));
            try {
                FakeReceiver.queueOfPackets.add(getPacket(command, obj, packetNumber++));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        ExecutorService service = Executors.newFixedThreadPool(THREAD_AMOUNT);
        while (queueOfPackets.size() != 0) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Descriptor.decrypt(queueOfPackets.poll());
                    } catch (Exception e) {
                    }
                }
            });
        }

        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    private byte[] getBrokenPacket() {
        final int PACKET_MAX_SIZE = 20;

        Random rn = new Random();
        final int packetSize = rn.nextInt(PACKET_MAX_SIZE) + 1;

        byte[] brokenPacketArray = new byte[packetSize];
        brokenPacketArray[rn.nextInt(packetSize)] = Packet.getBMagic();

        return brokenPacketArray;
    }

    private byte[] getPacket(Command command, JSONObject object, int userID) throws UnsupportedEncodingException {

        byte[] messageInfo = object.toString().getBytes("utf-8");

        final int COMMAND_SIZE = messageInfo.length;
        final int MESSAGE_SIZE = Integer.BYTES + Integer.BYTES + COMMAND_SIZE;
        final int PACKET_DATA_SIZE = Byte.BYTES + Byte.BYTES
                + Long.BYTES + Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_DATA_SIZE + Short.BYTES
                + MESSAGE_SIZE + Short.BYTES);

        ByteBuffer bufferPkt = ByteBuffer.allocate(PACKET_DATA_SIZE);

        bufferPkt.put(Packet.getBMagic())
                .put((byte) 0)
                .putLong(userID)
                .putInt(MESSAGE_SIZE);

        short wCrc16Pkt = CRC16.getCRC16(bufferPkt.array());
        buffer.put(bufferPkt.array())
                .putShort(wCrc16Pkt);

        ByteBuffer bufferMsg = ByteBuffer.allocate(MESSAGE_SIZE);
        bufferMsg.putInt(command.ordinal())
                .putInt(userID)
                .put(messageInfo);

        short wCrc16Msg = CRC16.getCRC16(bufferMsg.array());
        buffer.put(bufferMsg.array())
                .putShort(wCrc16Msg);

        return buffer.array();
    }
}
