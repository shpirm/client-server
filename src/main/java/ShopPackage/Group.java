package ShopPackage;

import java.util.ArrayList;
import java.util.Objects;

public class Group {

    private String name;

    Group(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        ArrayList<Product> arrayList = new ArrayList<Product>();
        for (Product product : Shop.productList)
            if (Objects.equals(product.getGroupName(), this.name))
                arrayList.add(product);

        return "Group {" +
                "name = '" + name + '\'' +
                ", products = " + arrayList +
                '}';
    }
}
