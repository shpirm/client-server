package Shop;

import java.util.ArrayList;

public class Group {

    private int id;

    private String name;
    private ArrayList<Integer> productsID;

    public Group(int id, String name) {
        this.id = id;
        this.name = name;

        productsID = new ArrayList<>();
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

    public ArrayList<Integer> getProductsID() {
        return productsID;
    }

    public void setProductsID(ArrayList<Integer> productsID) {
        this.productsID = productsID;
    }


    @Override
    public String toString() {
        return "Group { " +
                "id = " + id +
                ", name = '" + name + '\'' +
                ", productsID = " + productsID +
                " }";
    }
}
