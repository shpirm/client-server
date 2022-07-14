import Shop.ShopDatabase;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class DatabaseTest {
    @Test
    public void methodsTest() throws SQLException {
        ShopDatabase db = new ShopDatabase();
        db.initialization();

        db.deleteAllProducts();
        db.deleteAllGroups();

        db.insertGroup("Group");
        System.out.println(db.readGroup("Group").toString());

        db.updateGroupName("Group", "NewGroup");
        System.out.println(db.getGroupList("GroupID").toString());

        db.insertProduct("Product", 100, 500, "NewGroup");
        System.out.println(db.readProduct("Product").toString());

        db.insertProduct("Product1", 100, 500, "NewGroup");
        System.out.println(db.readProduct("Product1").toString());

        System.out.println(db.readGroup("NewGroup").toString());

        db.updateProductAmount("Product", - 200);
        System.out.println(db.getGroupList("GroupID").toString());
        System.out.println(db.getProductList("ProductName").toString());

        db.deleteProduct("Product1");

        System.out.println(db.getGroupList("GroupID").toString());
        System.out.println(db.getProductList("ProductName").toString());
    }
}
