package ServerClient;

import Shop.Command;
import Structure.FakeReceiver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StoreServerTCP extends Thread {

    private boolean shutdown;
    private ServerSocket serverSocket;
    private FakeReceiver receiver;

    public static Map<Integer, Socket> clientMap = new ConcurrentHashMap<>();

    public StoreServerTCP(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        receiver = new FakeReceiver();
    }


    public void run() {
        shutdown = false;
        receiver.start();

        while (!shutdown) {
            try {
                new ClientHandler(serverSocket.accept()).start();
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void doStop() throws IOException {
        shutdown = true;
        receiver.doStop();
        serverSocket.close();
    }

    private class ClientHandler extends Thread {
        private static int staticUserID = 0;
        private final int userID;
        private final Socket clientSocket;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.userID = staticUserID++;

            clientMap.put(userID, clientSocket);
        }

        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String inputLine;
            while (true) {
                try {
                    inputLine = in.readLine();
                    if (inputLine == null) break;
                    if (inputLine.equals(Command.CLOSE.toString())) {
                        clientMap.remove(userID);

                        in.close();
                        clientSocket.close();
                        break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                receiver.receiveMessage(parseStringToByte(inputLine));
            }
        }
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