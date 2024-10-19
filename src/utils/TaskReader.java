package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AtomicTask;
import entities.Order;
import entities.OrderList;
import entities.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TaskReader {
    private OrderList orderList;
    private ArrayList<AtomicTask> atomicTasksList;

    public TaskReader() {
        orderList = new OrderList();
        atomicTasksList = new ArrayList<>();
    }

    public void retrieveOrders() {
        ObjectMapper om = new ObjectMapper();

        try {
            orderList = om.readValue(new File("src/resources/set5.json"), OrderList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Order order : orderList.getOrders()) {
            String orderNumber = order.getOrderNumber();
            for (Task task : order.getTasks()) {
                for (int i = 0; i < task.getQuantity(); i++) {
                    atomicTasksList.add(new AtomicTask(orderNumber, task.getTaskId(), task.getLength(), task.getWidth(), task.getHeight(), task.getFilament()));
                }
            }
        }
    }

    public OrderList getOrderList() {
        return orderList;
    }

    public ArrayList<AtomicTask> getAtomicTasksList() {
        return atomicTasksList;
    }
}
