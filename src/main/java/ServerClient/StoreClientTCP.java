package ServerClient;

import Packet.CRC16;
import Packet.Packet;
import Shop.UserCommand;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class StoreClientTCP {
    private static int userID = 0;

    private static int packetID = 0;
    private int clientUserId;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        this.clientUserId = userID++;
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(UserCommand command, JSONObject object) throws IOException, InterruptedException {
        out.println(Arrays.toString(getPacket(command, object, clientUserId, packetID++)));
        Thread.sleep(100);

        if (in.ready()) System.out.println("User " + clientUserId + " received: " + in.readLine());
        else {
            System.out.println("User " + clientUserId + ": No answer! Tried again...");
            Thread.sleep(500);
            if (!in.ready()) System.out.println("User " + clientUserId + ": No final answer!");
            else System.out.println("User " + clientUserId + " received: " + in.readLine());
        }
    }

    public void stopSocket() {
        out.println(UserCommand.CLOSE);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
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
}
