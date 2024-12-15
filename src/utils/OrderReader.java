package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import entities.AtomicTask;
import entities.Order;
import entities.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class OrderReader {
    private String filePath;
    private OrderList orderList;
    private ArrayList<AtomicTask> atomicTasksList;

    public OrderReader(int orderCount) {
        filePath = "src/resources/set" + orderCount + ".json";
        orderList = new OrderList();
        atomicTasksList = new ArrayList<>();
    }

    public void retrieveOrders() {
        ObjectMapper om = new ObjectMapper();

        try {
            orderList = om.readValue(new File(filePath), OrderList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int atomicTaskCount = 0;
        for (Order order : orderList.getOrders()) {
            int orderId = order.getOrderId();
            int deadline = order.getDeadline();
            for (Task task : order.getTasks()) {
                for (int i = 0; i < task.getQuantity(); i++) {
                    atomicTaskCount++;
                    atomicTasksList.add(new AtomicTask(orderId, deadline, task.getTaskId(), task.getLength(), task.getWidth(), task.getHeight(), task.getFilament(), atomicTaskCount));
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
