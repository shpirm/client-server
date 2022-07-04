package Shop;

import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.ArrayList;

public class ShopDatabase {
    private Connection con;
    private final String name = "Shop";

    public void initialization() {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + name);
            PreparedStatement st = con.prepareStatement(
                    "CREATE TABLE if NOT EXISTS 'ProductGroup' (" +
                            "'GroupID' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'GroupName' VARCHAR(20) NOT NULL UNIQUE)");

            st.executeUpdate();

            st = con.prepareStatement(
                    "CREATE TABLE if NOT EXISTS 'Product' (" +
                            "'ProductID' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'ProductName' VARCHAR(20) NOT NULL UNIQUE, " +
                            "'ProductAmount' INT DEFAULT 0, " +
                            "'ProductPrice' DECIMAL DEFAULT 0, " +
                            "'GroupID' INTEGER DEFAULT 0, " +
                            "CONSTRAINT 'GroupID' " +
                            "FOREIGN KEY ('GroupID') " +
                            "REFERENCES 'Group'('GroupID'))");

            st.executeUpdate();

//            this.deleteAllGroups();
//            this.deleteAllProducts();
        } catch (ClassNotFoundException e) {
            System.out.println("Не знайшли драйвер JDBC");
            e.printStackTrace();
            System.exit(0);

        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            e.printStackTrace();
        }
    }

    public void insertProduct(String name, int amount, double price, String groupName) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM 'ProductGroup' WHERE GroupName = ?");
        statement.setString(1, groupName);
        ResultSet res = statement.executeQuery();

        statement = con.prepareStatement(
                "INSERT INTO Product(ProductName, ProductAmount, ProductPrice, GroupID) " +
                        "VALUES (?, ?, ?, ?)");

        statement.setString(1, name);
        statement.setInt(2, amount);
        statement.setDouble(3, price);
        statement.setInt(4, res.getInt("GroupID"));

        statement.executeUpdate();
        statement.close();
    }

    public Product readProduct(String name) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "SELECT * FROM Product WHERE ProductName = ?");
        statement.setString(1, name);
        ResultSet res = statement.executeQuery();

        Product product = new Product(res.getInt("ProductID"),
                res.getString("ProductName"),
                res.getInt("ProductAmount"),
                res.getDouble("ProductPrice"),
                res.getInt("GroupID"));

        res.close();
        statement.close();

        return product;
    }

    public ArrayList<Product> getProductList(String criteria) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM Product ORDER BY ? ASC");
        statement.setString(1, criteria);
        ResultSet res = statement.executeQuery();

        ArrayList<Product> arrayList = new ArrayList<>();

        while (res.next()) {
            arrayList.add(new Product(
                    res.getInt("ProductID"),
                    res.getString("ProductName"),
                    res.getInt("ProductAmount"),
                    res.getDouble("ProductPrice"),
                    res.getInt("GroupID")
            ));
        }
        res.close();
        statement.close();
        return arrayList;
    }

    public void updateProductName(String oldName, String newName) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "UPDATE Product SET ProductName = ? WHERE ProductName = ?");

        statement.setString(2, oldName);
        statement.setString(1, newName);

        statement.executeUpdate();
        statement.close();
    }

    public void updateProductAmount(String name, int amount) throws SQLiteException, SQLException {
        PreparedStatement oldStatement = con.prepareStatement(
                "SELECT * FROM Product WHERE ProductName = ?");
        oldStatement.setString(1, name);
        ResultSet res = oldStatement.executeQuery();

        PreparedStatement statement = con.prepareStatement(
                "UPDATE Product SET ProductAmount = ? WHERE ProductName = ?");

        statement.setString(2, name);
        statement.setInt(1, res.getInt("ProductAmount") + amount);

        statement.executeUpdate();
        statement.close();
    }

    public void updateProductPrice(String name, double price) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "UPDATE Product SET ProductPrice = ? WHERE ProductName = ?");

        statement.setString(2, name);
        statement.setDouble(1, price);

        statement.executeUpdate();
        statement.close();
    }

    public void updateProductGroup(String productName, String groupName) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "SELECT * FROM 'ProductGroup' WHERE GroupName = ?");

        statement.setString(1, groupName);
        ResultSet resGroup = statement.executeQuery();

        statement = con.prepareStatement(
                "UPDATE Product SET GroupID = ? WHERE ProductName = ?");

        statement.setString(2, productName);
        statement.setInt(1, resGroup.getInt("GroupID"));

        statement.executeUpdate();

        resGroup.close();
        statement.close();
    }

    public void deleteAllProducts() throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement("DELETE FROM Product");
        statement.executeUpdate();

        statement.close();
    }

    public void deleteProduct(String name) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "DELETE FROM Product WHERE ProductName = ?");

        statement.setString(1, name);
        statement.executeUpdate();
        statement.close();
    }

    public void insertGroup(String name) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "INSERT INTO 'ProductGroup'(GroupName) VALUES (?)");

        statement.setString(1, name);

        statement.executeUpdate();
        statement.close();
    }

    public Group readGroup(String name) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "SELECT * FROM 'ProductGroup' WHERE GroupName = ?");
        statement.setString(1, name);
        ResultSet resGroup = statement.executeQuery();

        Group group = new Group(resGroup.getInt("GroupID"),
                resGroup.getString("GroupName"));

        statement = con.prepareStatement(
                "SELECT * FROM Product WHERE GroupID = ?");
        statement.setInt(1, group.getId());
        ResultSet resProduct = statement.executeQuery();

        while (resProduct.next()) {
            group.getProductsID().add(resProduct.getInt("ProductID"));
        }

        resGroup.close();
        resProduct.close();
        statement.close();

        return group;
    }

    public ArrayList<Group> getGroupList(String criteria) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * FROM 'ProductGroup' ORDER BY ? ASC");
        statement.setString(1, criteria);
        ResultSet res = statement.executeQuery();

        ArrayList<Group> arrayList = new ArrayList<>();

        while (res.next()) {
            Group group = new Group(
                    res.getInt("GroupID"),
                    res.getString("GroupName"));

            statement = con.prepareStatement(
                    "SELECT * FROM Product WHERE GroupID = ?");
            statement.setInt(1, group.getId());
            ResultSet resProduct = statement.executeQuery();

            while (resProduct.next()) {
                group.getProductsID().add(resProduct.getInt("ProductID"));
            }
            arrayList.add(group);
        }
        return arrayList;
    }

    public void updateGroupName(String oldName, String newName) throws SQLiteException, SQLException {

        PreparedStatement statement = con.prepareStatement(
                "UPDATE 'ProductGroup' SET GroupName = ? WHERE GroupName = ?");

        statement.setString(2, oldName);
        statement.setString(1, newName);

        statement.executeUpdate();
        statement.close();
    }

    public void deleteAllGroups() throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement("DELETE FROM 'ProductGroup'");
        statement.executeUpdate();

        statement.close();

    }

    public void deleteGroup(String name) throws SQLiteException, SQLException {
        PreparedStatement statement = con.prepareStatement(
                "UPDATE Product SET GroupID = 0 WHERE GroupID = (" +
                        "SELECT GroupID FROM 'ProductGroup' WHERE GroupName = ?);");

        statement.setString(1, name);
        statement.executeUpdate();

        statement = con.prepareStatement(
                "DELETE FROM 'ProductGroup' WHERE GroupName = ?;");

        statement.setString(1, name);
        statement.executeUpdate();

        statement.close();
    }

    public static void main(String[] args) throws SQLException {
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

        db.updateProductAmount("Product", -200);
        System.out.println(db.getGroupList("GroupID").toString());
        System.out.println(db.getProductList("ProductName").toString());

        db.deleteProduct("Product1");

        System.out.println(db.getGroupList("GroupID").toString());
        System.out.println(db.getProductList("ProductName").toString());
    }
}
