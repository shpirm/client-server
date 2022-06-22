package StructurePackage;

import PacketPackage.Packet;
import ShopPackage.Command;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Encryptor {
    public static void encrypt(Packet packet) throws Exception {
        ByteArrayOutputStream byteArrOutStr = new ByteArrayOutputStream();
        DataOutputStream dataOutStr = new DataOutputStream(byteArrOutStr);


        try {
            dataOutStr.writeByte(Packet.getBMagic());
            dataOutStr.writeByte(packet.getBSrc());

            dataOutStr.writeLong(packet.getBPktId());
            dataOutStr.writeInt(packet.getWLen());

            dataOutStr.writeShort(packet.getWCrc16());

            dataOutStr.writeInt(packet.getBMsg().getCType());
            dataOutStr.writeInt(packet.getBMsg().getBUserId());
            dataOutStr.write(packet.getBMsg().getMessage());

            dataOutStr.writeShort(packet.getWCrc16Msg());

            dataOutStr.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Sender.sendMessage(packet.getBMsg().getBUserId(),
                new String(packet.getBMsg().getMessage()),
                Command.values()[packet.getBMsg().getCType()]);
    }
}
