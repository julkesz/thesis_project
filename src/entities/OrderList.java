package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderList implements Serializable {
    private ArrayList<Order> orders;

    public OrderList() {
    }

    @Override
    public String toString() {
        return "OrderList{" +
                "orders=" + orders +
                '}';
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }
}
