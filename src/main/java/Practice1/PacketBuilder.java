package Practice1;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class PacketBuilder {

    public byte[] encodePacket(ArrayList<Packet> packets) throws Exception {

        ByteArrayOutputStream byteArrOutStr = new ByteArrayOutputStream();
        DataOutputStream dataOutStr = new DataOutputStream(byteArrOutStr);

        for (Packet packet : packets)
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
        return byteArrOutStr.toByteArray();
    }

    public ArrayList<Packet> decodePacket(byte[] bytes) throws Exception {
        ArrayList<Packet> packets = new ArrayList<Packet>();
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

                if (pktWCrc16 != packet.getWCrc16()) {
                    throw new Exception();
                } else {
                    packet.setBMsg(new Message(buffer, wLen));

                    packet.setWCrc16Msg(buffer.getShort());
                    short pktWCrc16Msg = packet.getBMsg().getCrc16Msg();

                    if (pktWCrc16Msg != packet.getWCrc16Msg()) {
                        throw new Exception();
                    } else {
                        packets.add(packet);
                    }
                }
            }
        }
        System.out.println("Packets found: " + packets.size());
        return packets;
    }
}