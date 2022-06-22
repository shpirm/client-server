import ShopPackage.Product;
import ShopPackage.Shop;
import StructurePackage.FakeReceiver;
import org.junit.jupiter.api.Test;


public class ShopThreadsTest {

    @Test
    void test() throws Exception {
        long time = System.currentTimeMillis();

        FakeReceiver receiver = new FakeReceiver();
        receiver.receiveMessage();

//        System.out.println(Shop.groupList);
//        System.out.println(Shop.productList);
//        System.out.println(Shop.productList.size());
//        System.out.println(Shop.groupList.size());

        System.out.println("Time = " + (System.currentTimeMillis() - time));

        assert(Shop.productList.size() ==
                FakeReceiver.PRODUCT_NUMBER);
        assert(Shop.groupList.size() ==
                FakeReceiver.PRODUCT_NUMBER);

        for (Product product : Shop.productList) {
            assert (product.getAmount() == FakeReceiver.PRODUCT_AMOUNT);
            assert (product.getPrice() == FakeReceiver.PRODUCT_PRICE);
        }
    }
}
