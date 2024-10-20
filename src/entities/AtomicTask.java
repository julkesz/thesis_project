package entities;

import java.io.Serializable;
import java.util.Objects;

public class AtomicTask implements Serializable {
    private String orderNumber;
    private String taskId;
    private int length;
    private int width;
    private int height;
    private int filament;

    public AtomicTask() {
    }

    public AtomicTask(String orderNumber, String taskId, int length, int width, int height, int filament) {
        this.orderNumber = orderNumber;
        this.taskId = taskId;
        this.length = length;
        this.width = width;
        this.height = height;
        this.filament = filament;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {this.height = height; }

    public int getFilament() {
        return filament;
    }

    public void setFilament(int filament) {
        this.filament = filament;
    }

    @Override
    public String toString() {
        return "AtomicTask{" +
                "orderNumber='" + orderNumber + '\'' +
                ", taskId='" + taskId + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", height=" + height +
                ", filament=" + filament +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicTask that = (AtomicTask) o;
        return length == that.length && width == that.width && height == that.height && filament == that.filament && Objects.equals(orderNumber, that.orderNumber) && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, taskId, length, width, height, filament);
    }
}
