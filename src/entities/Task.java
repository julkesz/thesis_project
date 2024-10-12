package entities;

import java.io.Serializable;

public class Task implements Serializable {
    private String taskId;
    private int length;
    private int width;
    private int quantity;
    private int executionTime;
    private int filament;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", length=" + length +
                ", width=" + width +
                ", quantity=" + quantity +
                ", executionTime=" + executionTime +
                ", filament=" + filament +
                '}';
    }
}