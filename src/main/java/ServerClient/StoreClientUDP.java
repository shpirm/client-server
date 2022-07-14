package ServerClient;

import Packet.CRC16;
import Packet.Packet;
import Packet.Message;
import Shop.UserCommand;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class StoreClientUDP {

    private static int userID = 0;

    private static int packetID = 0;

    private int clientUserId;
    private DatagramSocket socket;
    private InetAddress address;
    private byte[] message;
    private final int BUFFER_SIZE = 128;
    private byte[] buffer = new byte[BUFFER_SIZE];

    public StoreClientUDP() throws SocketException, UnknownHostException {
        this.clientUserId = userID++;
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public void sendMessage(UserCommand command, JSONObject object, int port) throws Exception {
        message = getPacket(command, object, clientUserId, packetID++);
        DatagramPacket packet
                = new DatagramPacket(message, message.length, address, port);

        final int ATTEMPTS = 3;
        int index = 0;
        while (true) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("User " + clientUserId + " sent packet (attempt = " + (index) + ")");
            try {
                socket.setSoTimeout(1000);
                socket.receive(packet);

                ByteBuffer received = ByteBuffer.wrap(
                        packet.getData(), 0, packet.getLength());

                System.out.println("User " + clientUserId + " received packet: "
                        + parseMessageIntoString(received.array()));
                break;
            } catch (SocketTimeoutException e) {
                if (index == ATTEMPTS) {
                    System.out.println("User " + clientUserId + " could not receive an answer. ");
                    break;
                } else index++;
            }

        }
    }

    public void close() {
        socket.close();
    }

    private static byte[] getPacket(UserCommand command, JSONObject object, int userID, int packetID) throws UnsupportedEncodingException {

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
                .putLong(packetID)
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

    private String parseMessageIntoString(byte[] message) throws Exception {

        String answer = "{ ";
        ByteBuffer buffer = ByteBuffer.wrap(message);
        Packet packet = new Packet();

        buffer.get();
        answer += "Src = " + buffer.get();
        answer += ", PktId = " + buffer.getLong();

        int wLen = buffer.getInt();
        answer += ", Len = " + wLen;

        buffer.getShort();

        UserCommand command = UserCommand.values()[buffer.getInt()];
        answer += " { Command = " + command.toString();
        answer += ", User = " + buffer.getInt();

        answer += ", Message = " + Arrays.toString(buffer.array());
        return answer;
    }
}
