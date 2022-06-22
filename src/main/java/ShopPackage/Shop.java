package ShopPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Shop {
    public static ArrayList<Group> groupList = new ArrayList<>();
    public static ArrayList<Product> productList = new ArrayList<>();

    public static void changeGroupName(String oldName, String newName) {
        for (Group group : Shop.groupList) {
            if (Objects.equals(group.getName(), oldName)) {
                group.setName(newName);
                break;
            }
        }
    }

    public static void createGroup(String name, String... productsNames) {
        Group group = new Group(name);
        for (Product product : Shop.productList) {
            if (Arrays.asList(productsNames).contains(product.getName()))
                product.setGroupName(name);
        }
        Shop.groupList.add(group);
    }

    public static void setProductPrice(String productName, int price) {
        Product product = findProductByName(productName);
        if (product != null) product.setPrice(price);
    }

    public static void deleteProduct(String productName) {
        Shop.productList.removeIf(product -> (Objects.equals(product.getName(), productName)));
    }

    public static void productAmountDecrease(String productName, int amount) {
        Product product = findProductByName(productName);
        if (product != null)
            product.setAmount(Math.max(product.getAmount() - amount, 0));
    }

    public static void productAmountIncrease(String productName, int amount) {
        Product product = findProductByName(productName);
        if (product != null)
            product.setAmount(product.getAmount() + amount);
    }

    public static void addProduct(String productName, int amount) {
        Product product = findProductByName(productName);
        if (product == null) Shop.productList.add(new Product(productName, amount));
    }

    public static void getProductInfo(String productName) {
        Product product = findProductByName(productName);
        if (product != null) System.out.println(product);
    }
    private static Product findProductByName(String productName) {
        for (Product product : Shop.productList) {
            if (Objects.equals(product.getName(), productName)) {
                return product;
            }
        }
        return null;
    }
}
