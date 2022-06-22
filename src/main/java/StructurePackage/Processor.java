package StructurePackage;

import PacketPackage.Message;
import PacketPackage.Packet;
import ShopPackage.Command;
import ShopPackage.Shop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class Processor {
    public static void process(Packet packet) throws Exception {
        Command command = Command.values()[packet.getBMsg().getCType()];

        switch (command) {
            case PRODUCT_ADD -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.addProduct(productName, amount);
                }
            }
            case PRODUCT_INFO -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                synchronized (Shop.productList) {
                    Shop.getProductInfo(productName);
                }
            }
            case PRODUCT_INCREASE -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.productAmountIncrease(productName, amount);
                }
            }
            case PRODUCT_DECREASE -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                int amount = (int) jsonObject.get("amount");
                synchronized (Shop.productList) {
                    Shop.productAmountDecrease(productName, amount);
                }
            }
            case PRODUCT_DELETE -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                synchronized (Shop.productList) {
                    Shop.deleteProduct(productName);
                }
            }
            case PRODUCT_PRICE -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String productName = (String) jsonObject.get("productName");
                int price = (int) jsonObject.get("price");
                synchronized (Shop.productList) {
                    Shop.setProductPrice(productName, price);
                }
            }
            case GROUP_CREATE -> {
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String groupName = (String) jsonObject.get("groupName");

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
                JSONObject jsonObject = new JSONObject(new String(packet.getBMsg().getMessage()));
                String oldName = (String) jsonObject.get("oldName");
                String newName = (String) jsonObject.get("newName");
                synchronized (Shop.groupList) {
                    Shop.changeGroupName(oldName, newName);
                }
            }
        }
        Encryptor.encrypt(sendAnswer(packet));
    }

    static Packet sendAnswer(Packet packet) throws Exception {

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
