package entities;

import java.io.Serializable;

public class Task implements Serializable {
    private int length;
    private int width;
    private int quantity;
    private long executionTime;
    private int filament;

    @Override
    public String toString() {
        return "Task{" +
                "length=" + length +
                ", width=" + width +
                ", quantity=" + quantity +
                ", executionTime=" + executionTime +
                ", filament=" + filament +
                '}';
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

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecution_time(long executionTime) {
        this.executionTime = executionTime;
    }

    public int getFilament() {
        return filament;
    }

    public void setFilament(int filament) {
        this.filament = filament;
    }

}