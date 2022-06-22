package StructurePackage;

import PacketPackage.Message;
import PacketPackage.Packet;

import java.nio.ByteBuffer;

public class Descriptor {

    public static void decrypt(byte[] bytes) throws Exception {

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
                        Processor.process(packet);
                    }
                }
            }
        }
    }
}

