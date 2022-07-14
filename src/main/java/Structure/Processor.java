package Structure;

import Packet.Message;
import Packet.Packet;
import Shop.ShopDatabase;
import Shop.UserCommand;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.sqlite.SQLiteException;


import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Processor extends Thread {

    private boolean stop;
    private ExecutorService service;
    private Encryptor encryptor;

    private ShopDatabase db;

    private static final int THREAD_AMOUNT = 15;
    private ConcurrentLinkedQueue<Packet> queueOfPackets;

    public Processor() {
        db = new ShopDatabase();
        db.initialization();
    }

    public void run() {
        stop = true;

        queueOfPackets = new ConcurrentLinkedQueue<>();
        service = Executors.newFixedThreadPool(THREAD_AMOUNT);

        encryptor = new Encryptor();
        encryptor.start();

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
        encryptor.doStop();
    }

    public void process(Packet packet) {
        queueOfPackets.add(packet);
    }

    private void command(Packet packet) throws Exception {
        UserCommand command = UserCommand.values()[packet.getBMsg().getCType()];
        JSONObject jsonObject = new JSONObject(IOUtils.toString(packet.getBMsg().getMessage()));

        UserCommand answerCommand = UserCommand.ANSWER;
        byte[] answer = new byte[0];

        try {
            switch (command) {
                case PRODUCT_INSERT -> {
                    synchronized (db) {
                        db.insertProduct(
                                String.valueOf(jsonObject.get("ProductName")),
                                jsonObject.getInt("ProductAmount"),
                                jsonObject.getDouble("ProductPrice"),
                                String.valueOf(jsonObject.get("GroupName")));
                    }
                }
                case PRODUCT_READ -> {
                    synchronized (db) {
                        answer = parseStringToByte(db.readProduct(
                                String.valueOf(jsonObject.get("ProductName"))).toString());
                    }
                }
                case PRODUCT_DELETE -> {
                    synchronized (db) {
                        db.deleteProduct(
                                String.valueOf(jsonObject.get("ProductName")));
                    }
                }
                case PRODUCT_LIST -> {
                    synchronized (db) {
                        answer = parseStringToByte(db.getProductList(
                                String.valueOf(jsonObject.get("Criteria"))).toString());
                    }
                }
                case PRODUCT_NAME -> {
                    synchronized (db) {
                        db.updateProductName(
                                String.valueOf(jsonObject.get("ProductName")),
                                String.valueOf(jsonObject.get("NewProductName")));
                    }
                }
                case PRODUCT_PRICE -> {
                    synchronized (db) {
                        db.updateProductPrice(
                                String.valueOf(jsonObject.get("ProductName")),
                                jsonObject.getDouble("ProductPrice"));
                    }
                }
                case PRODUCT_AMOUNT -> {
                    synchronized (db) {
                        db.updateProductAmount(
                                String.valueOf(jsonObject.get("ProductName")),
                                jsonObject.getInt("ProductAmount"));
                    }
                }
                case PRODUCT_GROUP -> {
                    synchronized (db) {
                        db.updateProductGroup(
                                String.valueOf(jsonObject.get("ProductName")),
                                String.valueOf(jsonObject.get("GroupName")));
                    }
                }
                case GROUP_INSERT -> {
                    synchronized (db) {
                        db.insertGroup(
                                String.valueOf(jsonObject.get("GroupName")));
                    }
                }
                case GROUP_READ -> {
                    synchronized (db) {
                        answer = parseStringToByte(db.readGroup(
                                String.valueOf(jsonObject.get("GroupName"))).toString());
                    }
                }
                case GROUP_DELETE -> {
                    synchronized (db) {
                        db.deleteGroup(
                                String.valueOf(jsonObject.get("GroupName")));
                    }
                }
                case GROUP_LIST -> {
                    synchronized (db) {
                        answer = parseStringToByte(db.getGroupList(
                                String.valueOf(jsonObject.get("Criteria"))).toString());
                    }
                }
                case GROUP_NAME -> {
                    synchronized (db) {
                        db.updateGroupName(
                                String.valueOf(jsonObject.get("GroupName")),
                                String.valueOf(jsonObject.get("NewGroupName")));
                    }
                }
                default -> throw new SQLException("Unexpected command. ");
            }
        } catch (SQLiteException e) {
            answerCommand = UserCommand.ERROR;
        } catch (SQLException e) {
            answerCommand = UserCommand.ERROR;
        }
        encryptor.encrypt(sendAnswer(packet, answerCommand, answer));
    }

    private Packet sendAnswer(Packet packet, UserCommand command, byte[] message) throws Exception {

        final int COMMAND_SIZE = message.length;
        final int MESSAGE_SIZE = Integer.BYTES + Integer.BYTES + COMMAND_SIZE;

        ByteBuffer bufferMsg = ByteBuffer.allocate(MESSAGE_SIZE);
        bufferMsg.putInt(command.ordinal())
                .putInt(packet.getBMsg().getBUserId())
                .put(message);

        bufferMsg.position(0);
        Message messageOK = new Message(bufferMsg, MESSAGE_SIZE);
        Packet packetAnswer = new Packet();

        packetAnswer.setBSrc(packet.getBSrc());
        packetAnswer.setBPktId(packet.getBPktId());
        packetAnswer.setWLen(MESSAGE_SIZE);

        short wCrc16Pkt = packetAnswer.getCrc16Pkt();
        packetAnswer.setWCrc16(wCrc16Pkt);

        packetAnswer.setBMsg(messageOK);
        short wCrc16Msg = packetAnswer.getBMsg().getCrc16Msg();
        packetAnswer.setWCrc16Msg(wCrc16Msg);

        return packetAnswer;
    }

    private byte[] parseStringToByte(String str) {
        String[] byteValues = str.substring(1, str.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];

        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }
        return bytes;
    }

}
