package entities;

import java.io.Serializable;
import java.util.Objects;

public class AtomicTask implements Serializable {
    private int orderId;
    private int deadline;
    private int taskId;
    private int length;
    private int width;
    private int height;
    private int filament;
    private int atomicTaskId;

    public AtomicTask() {
    }

    public AtomicTask(int orderId, int deadline, int taskId, int length, int width, int height, int filament, int atomicTaskId) {
        this.orderId = orderId;
        this.deadline = deadline;
        this.taskId = taskId;
        this.length = length;
        this.width = width;
        this.height = height;
        this.filament = filament;
        this.atomicTaskId = atomicTaskId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
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

    public int getAtomicTaskId() {
        return atomicTaskId;
    }

    public void setAtomicTaskId(int atomicTaskId) {
        this.atomicTaskId = atomicTaskId;
    }

    @Override
    public String toString() {
        return "AtomicTask{" +
                "orderId='" + orderId + '\'' +
                ", deadline=" + deadline +
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
        return length == that.length && width == that.width && height == that.height && filament == that.filament && deadline == that.deadline && orderId==that.orderId  && taskId==that.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, deadline, taskId, length, width, height, filament);
    }
}
