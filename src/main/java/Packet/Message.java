package Packet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {

    private final String KEY = "Some random keys";
    private int cType;
    private int bUserId;

    private byte[] message;

    public Message(ByteBuffer buffer, int wLen) throws Exception {
        this.cType = buffer.getInt();
        this.bUserId = buffer.getInt();

        int arrSize = wLen - Integer.BYTES * 2;
        byte[] msgInfo = new byte[arrSize];
        buffer.get(msgInfo, 0, arrSize);
        this.message = getEncryptedBytes(msgInfo);
    }

    public short getCrc16Msg() throws Exception {
        byte[] decryptedArray = getDecryptedBytes(message);
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + decryptedArray.length);

        buffer.putInt(this.cType)
                .putInt(this.bUserId)
                .put(decryptedArray);

        return CRC16.getCRC16(buffer.array());
    }

    private byte[] getEncryptedBytes(byte[] originalArray) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(originalArray);
    }

    private byte[] getDecryptedBytes(byte[] encryptedArray) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), "AES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedArray);
    }

    public int getCType() { return cType; }
    public int getBUserId() { return bUserId; }
    public byte[] getMessage() throws Exception { return getDecryptedBytes(message); }

    @Override
    public String toString() {
        return "Message {" +
                "cType = " + cType +
                ", bUserId = " + bUserId +
                ", message = " + Arrays.toString(message) +
                '}';
    }
}
