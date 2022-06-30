package Structure;

import Packet.Message;
import Packet.Packet;
import Shop.Command;
import Shop.Shop;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;


import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Processor extends Thread {

    private boolean stop;
    private ExecutorService service;
    private Encryptor encryptor;

    private static final int THREAD_AMOUNT = 3;

    private ConcurrentLinkedQueue<Packet> queueOfPackets;

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
        Command command = Command.values()[packet.getBMsg().getCType()];
        JSONObject jsonObject = new JSONObject(IOUtils.toString(packet.getBMsg().getMessage()));

        switch (command) {
            case PRODUCT_ADD -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.addProduct(productName, amount);
                }
            }
            case PRODUCT_INFO -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                synchronized (Shop.productList) {
                    Shop.getProductInfo(productName);
                }
            }
            case PRODUCT_INCREASE -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.productAmountIncrease(productName, amount);
                }
            }
            case PRODUCT_DECREASE -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.productAmountDecrease(productName, amount);
                }
            }
            case PRODUCT_DELETE -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                synchronized (Shop.productList) {
                    Shop.deleteProduct(productName);
                }
            }
            case PRODUCT_PRICE -> {
                String productName = String.valueOf(jsonObject.get("productName"));
                int price = (int) jsonObject.get("price");
                synchronized (Shop.productList) {
                    Shop.setProductPrice(productName, price);
                }
            }
            case GROUP_CREATE -> {
                String groupName = String.valueOf(jsonObject.get("groupName"));

                JSONArray jsonArray = jsonObject.getJSONArray("productNames");
                String[] productNames = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++)
                    productNames[i] = jsonArray.getString(i);
                synchronized (Shop.groupList) {
                    synchronized (Shop.productList) {
                        Shop.createGroup(groupName, productNames);
                    }
                }
            }
            case GROUP_NAME_CHANGE -> {
                String oldName = String.valueOf(jsonObject.get("oldName"));
                String newName = String.valueOf(jsonObject.get("newName"));
                synchronized (Shop.groupList) {
                    Shop.changeGroupName(oldName, newName);
                }
            }
        }
        encryptor.encrypt(sendAnswer(packet));
    }

    private Packet sendAnswer(Packet packet) throws Exception {

        String OK = "OK";
        final int COMMAND_SIZE = OK.getBytes().length;
        final int MESSAGE_SIZE = Integer.BYTES + Integer.BYTES + COMMAND_SIZE;

        ByteBuffer bufferMsg = ByteBuffer.allocate(MESSAGE_SIZE);
        bufferMsg.putInt(packet.getBMsg().getCType())
                .putInt(packet.getBMsg().getBUserId())
                .put(OK.getBytes());

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
}
