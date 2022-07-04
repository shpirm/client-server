package Shop;

public class Product {

    private int id;

    private String name;

    private int amount;
    private double price;

    private int groupID;

    public Product(int id, String name, int amount, double price, int groupID) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.groupID = groupID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    @Override
    public String  toString() {
        return "Product {" +
                " id = " + id +
                ", name = '" + name + '\'' +
                ", amount = " + amount +
                ", price = " + price +
                ", groupID = " + groupID +
                " }";
    }
}
