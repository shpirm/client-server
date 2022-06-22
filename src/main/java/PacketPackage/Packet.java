package PacketPackage;

import java.nio.ByteBuffer;

public class Packet {
    private static final byte bMagic = 0x13;

    private byte bSrc;

    private long bPktId;
    private int wLen;

    private short wCrc16;
    private Message bMsg;
    private short wCrc16Msg;

    @Override
    public String toString() {
        return "Packet {" +
                "bMagic = " + bMagic +
                ", bSrc = " + bSrc +
                ", bPktId = " + bPktId +
                ", wLen = " + wLen +
                ", wCrc16 = " + wCrc16 +
                ", bMsg = " + bMsg +
                ", wCrc16Msg = " + wCrc16Msg +
                '}';
    }

    public short getCrc16Pkt() {
        ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Byte.BYTES + Long.BYTES + Integer.BYTES);

        buffer.put(bMagic)
                .put(this.bSrc)
                .putLong(this.bPktId)
                .putInt(this.wLen);

        return CRC16.getCRC16(buffer.array());
    }

    public static byte getBMagic() {
        return bMagic;
    }

    public byte getBSrc() {
        return bSrc;
    }

    public long getBPktId() {
        return bPktId;
    }

    public int getWLen() {
        return wLen;
    }

    public short getWCrc16() {
        return wCrc16;
    }

    public Message getBMsg() {
        return bMsg;
    }

    public short getWCrc16Msg() {
        return wCrc16Msg;
    }

    public void setBSrc(byte bSrc) {
        this.bSrc = bSrc;
    }

    public void setBPktId(long bPktId) {
        this.bPktId = bPktId;
    }

    public void setWLen(int wLen) {
        this.wLen = wLen;
    }

    public void setWCrc16(short wCrc16) {
        this.wCrc16 = wCrc16;
    }

    public void setBMsg(Message bMsg) {
        this.bMsg = bMsg;
    }

    public void setWCrc16Msg(short wCrc16Msg) {
        this.wCrc16Msg = wCrc16Msg;
    }
}
