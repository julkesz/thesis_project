package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private String orderNumber;
    private String deadline;
    public ArrayList<Task> tasks;

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                ", deadline='" + deadline + '\'' +
                ", tasks=" + tasks +
                '}';
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }


}


