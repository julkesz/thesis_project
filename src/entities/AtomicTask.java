package entities;

import java.io.Serializable;
import java.util.Objects;

public class AtomicTask implements Serializable {
    private String orderNumber;
    private String deadline;
    private int length;
    private int width;
    private int executionTime;
    private int filament;

    public AtomicTask() {
    }

    public AtomicTask(String orderNumber, String deadline, int length, int width, int executionTime, int filament) {
        this.orderNumber = orderNumber;
        this.deadline = deadline;
        this.length = length;
        this.width = width;
        this.executionTime = executionTime;
        this.filament = filament;
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

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

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
                ", deadline='" + deadline + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", executionTime=" + executionTime +
                ", filament=" + filament +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtomicTask that = (AtomicTask) o;
        return length == that.length && width == that.width && executionTime == that.executionTime && filament == that.filament && Objects.equals(orderNumber, that.orderNumber) && Objects.equals(deadline, that.deadline);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, deadline, length, width, executionTime, filament);
    }
}
