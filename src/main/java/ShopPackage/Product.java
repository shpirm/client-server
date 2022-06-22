package ShopPackage;

public class Product {

    private final String name;

    private int amount;
    private int price;

    private String groupName;

    public Product(String name, int amount) {
        this.name = name;
        this.amount = amount;
        this.price = 0;
        this.groupName = "null";
    }

    public int getAmount() {
        return amount;
    }
    public String getGroupName() {
        return groupName;
    }
    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setPrice(int price) {
        this.price = price;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "Product {" +
                "name = '" + name + '\'' +
                ", amount = " + amount +
                ", price = " + price +
                ", groupName = " + groupName +
                '}';
    }
}
