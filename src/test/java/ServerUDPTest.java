import ServerClient.StoreClientUDP;
import ServerClient.StoreServerUDP;
import Shop.Command;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerUDPTest {
    public static Queue<Pair<Command, JSONObject>>
            queueOfPackets = new ConcurrentLinkedQueue<>();

    @Test
    public void controlledLoadServerTest() throws Exception {
        StoreServerUDP server = new StoreServerUDP(1337);
        server.start();

        final int MAX_CLIENT_NUMBER = 5;
        long time = System.currentTimeMillis();

        Thread loading = new LoadPacketsArray();
        loading.start();

        ArrayList<FakeClient> clientList = new ArrayList<>();
        for (int i = 0; i < MAX_CLIENT_NUMBER; i++) {
            FakeClient client = new FakeClient();
            clientList.add(client);
            client.start();
        }

        loading.join();
        for (FakeClient client : clientList) client.join();

        System.out.println("Time = " + (System.currentTimeMillis() - time));
        server.doStop();
    }

    @Test
    public void packetLostTest() throws Exception {
        final int PORT = 1337;
        StoreServerUDP server = new StoreServerUDP(PORT);
        server.start();

        long time = System.currentTimeMillis();

        StoreClientUDP client = new StoreClientUDP();
        client.sendMessage(Command.PRODUCT_ADD, new JSONObject(), PORT);

        System.out.println("Time = " + (System.currentTimeMillis() - time));
        server.doStop();
    }

    class FakeClient extends Thread {
        private final int PORT = 1337;
        private StoreClientUDP client;

        FakeClient() throws SocketException, UnknownHostException {
            client = new StoreClientUDP();
        }

        public void run() {
            while (true) {
                if (queueOfPackets.size() != 0) {
                    synchronized (queueOfPackets) {
                        Pair<Command, JSONObject> pair = queueOfPackets.poll();
                        try {
                            if (pair != null) {
                                client.sendMessage(pair.getKey(), pair.getValue(), PORT);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (queueOfPackets.size() == 0) break;
                }
            }
        }
    }

    class LoadPacketsArray extends Thread {

        final static int PRODUCT_NUMBER = 600;
        final static int PRODUCT_AMOUNT = 100;
        final static int PRODUCT_PRICE = 1000;
        final static int PRODUCT_AMOUNT_CHANGE = 10;
        final static int COMMAND_ID = 4;

        public void run() {

            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                JSONObject obj = new JSONObject();
                obj.put("productName", "product" + i);
                obj.put("amount", PRODUCT_AMOUNT);
                queueOfPackets.add(new Pair<>(Command.PRODUCT_ADD, obj));
            }

            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                for (int j = 0; j < COMMAND_ID; j++) {
                    JSONObject obj = new JSONObject();
                    Command command = Command.values()[j + 1];

                    switch (command) {
                        case PRODUCT_INFO, PRODUCT_DELETE -> {
                            obj.put("productName", "product" + i);
                        }
                        case PRODUCT_INCREASE, PRODUCT_DECREASE -> {
                            obj.put("productName", "product" + i);
                            obj.put("amount", PRODUCT_AMOUNT_CHANGE);
                        }
                        case PRODUCT_PRICE -> {
                            obj.put("productName", "product" + i);
                            obj.put("price", PRODUCT_PRICE);
                        }
                    }
                    queueOfPackets.add(new Pair<>(command, obj));
                }
            }

            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                JSONObject obj = new JSONObject();
                obj.put("groupName", "group" + i);

//            ArrayList<String> list = new ArrayList<String>();
//            for (int j = 0; j < GROUP_SIZE; j++)
//                list.add("product" + (i + j));
//            obj.put("productNames", new JSONArray(list));

                obj.put("productNames", new JSONArray(
                        new String[]{"product" + i}));
                queueOfPackets.add(new Pair<>(Command.GROUP_CREATE, obj));
            }
        }
    }
}