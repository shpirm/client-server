import ServerClient.StoreClientTCP;
import ServerClient.StoreServerTCP;
import Shop.ShopDatabase;
import Shop.UserCommand;
import javafx.util.Pair;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DatabaseTestTCP {
    public static Queue<Pair<UserCommand, JSONObject>>
            queueOfPackets = new ConcurrentLinkedQueue<>();

    @Test
    public void simpleTCPTest() throws IOException, InterruptedException, SQLException {
        StoreServerTCP server = new StoreServerTCP(1337);
        server.start();

        ShopDatabase db = new ShopDatabase();
        db.initialization();
        db.deleteAllProducts();
        db.deleteAllGroups();

        long time = System.currentTimeMillis();

        StoreClientTCP client = new StoreClientTCP();
        client.startConnection("127.0.0.1", 1337);

        client.sendMessage(UserCommand.GROUP_INSERT, new JSONObject().put("GroupName", "Group"));
        System.out.println("Check -- " + db.getGroupList("GroupName"));

        //Incorrect packet
        client.sendMessage(UserCommand.GROUP_NAME, new JSONObject().put("GroupName", "Group"));
        System.out.println("Check -- " + db.getGroupList("GroupName"));

        client.sendMessage(UserCommand.GROUP_NAME, new JSONObject()
                .put("GroupName", "Group")
                .put("NewGroupName", "NewGroup"));
        System.out.println("Check -- " + db.getGroupList("GroupName"));

        client.stopSocket();
        client.stopConnection();
        System.out.println("Time = " + (System.currentTimeMillis() - time));
        server.doStop();
    }

    @Test
    public void multithreadingTCPTest() throws IOException, InterruptedException, SQLException {
        StoreServerTCP server = new StoreServerTCP(1337);
        server.start();

        ShopDatabase db = new ShopDatabase();
        db.initialization();
        db.deleteAllProducts();
        db.deleteAllGroups();

        final int MAX_CLIENT_NUMBER = 5;
        long time = System.currentTimeMillis();

        Thread loading = new LoadPacketsThread();
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

        System.out.println(db.getGroupList("GroupName"));
        System.out.println(db.getProductList("ProductName"));
        server.doStop();
    }

    class FakeClient extends Thread {
        private StoreClientTCP client;

        FakeClient() throws IOException {
            client = new StoreClientTCP();
            client.startConnection("127.0.0.1", 1337);
        }

        public void run() {
            while (true) {
                if (queueOfPackets.size() != 0) {
                    synchronized (queueOfPackets) {
                        Pair<UserCommand, JSONObject> pair = queueOfPackets.poll();
                        try {
                            if (pair != null) {
                                client.sendMessage(pair.getKey(), pair.getValue());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (queueOfPackets.size() == 0) break;
                }
            }
            try {
                client.stopSocket();
                client.stopConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class LoadPacketsThread extends Thread {

        final static int PRODUCT_NUMBER = 500;
        final static int PRODUCT_AMOUNT = 100;
        final static double PRODUCT_PRICE = 100;
        final static int PRODUCT_AMOUNT_CHANGE = 15;
        final static int GROUP_SIZE = 5;
        final static int PRODUCT_COMMAND_AMOUNT = 4;

        public void run() {

            Random rn = new Random();

            for (int i = 0; i < PRODUCT_NUMBER / GROUP_SIZE; i++) {
                JSONObject obj = new JSONObject();
                obj.put("GroupName", "Group" + i);
                queueOfPackets.add(new Pair<>(UserCommand.GROUP_INSERT, obj));
            }
            for (int i = 0; i < PRODUCT_NUMBER / GROUP_SIZE; i++) {
                JSONObject obj = new JSONObject();
                int number = rn.nextInt(PRODUCT_NUMBER / GROUP_SIZE);
                obj.put("GroupName", "Group" + i);
                obj.put("NewGroupName", "NewGroup" + number);
                queueOfPackets.add(new Pair<>(UserCommand.GROUP_NAME, obj));
            }

            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                JSONObject obj = new JSONObject();
                obj.put("ProductName", "Product" + i);
                obj.put("ProductAmount", PRODUCT_AMOUNT);
                obj.put("ProductPrice", PRODUCT_PRICE);
                obj.put("GroupName", "Group" + i);
                queueOfPackets.add(new Pair<>(UserCommand.PRODUCT_INSERT, obj));
            }

            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                UserCommand command = UserCommand.values()[i % PRODUCT_COMMAND_AMOUNT];
                JSONObject obj = new JSONObject();
                switch (command) {
                    case PRODUCT_AMOUNT -> {
                        obj.put("ProductName", "Product" + i);
                        obj.put("ProductAmount", PRODUCT_AMOUNT_CHANGE);
                    }
                    case PRODUCT_PRICE -> {
                        obj.put("ProductName", "Product" + i);
                        obj.put("ProductPrice", PRODUCT_PRICE);
                    }
                    case PRODUCT_NAME -> {
                        obj.put("ProductName", "Product" + i);
                        obj.put("NewProductName", "NewProduct" + i);
                    }
                    case PRODUCT_GROUP -> {
                        obj.put("ProductName", "Product" + i);
                        obj.put("GroupName", "Group" + i);
                    }
                }
                queueOfPackets.add(new Pair<>(command, obj));
            }
            for (int i = 0; i < PRODUCT_NUMBER / GROUP_SIZE; i++) {
                JSONObject obj = new JSONObject();
                obj.put("GroupName", "Group" + i);
                queueOfPackets.add(new Pair<>(UserCommand.GROUP_DELETE, obj));
            }
            for (int i = 0; i < PRODUCT_NUMBER; i++) {
                JSONObject obj = new JSONObject();
                obj.put("ProductName", "Product" + i);
                queueOfPackets.add(new Pair<>(UserCommand.PRODUCT_DELETE, obj));
            }
        }
    }
}
