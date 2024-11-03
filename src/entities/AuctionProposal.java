package entities;


import java.io.Serializable;
import java.util.ArrayList;

public class AuctionProposal implements Serializable {
    private AtomicTask atomicTask;

    private int timeSlotNumber;

    private int executionTime;
    private float price;

    public AuctionProposal(AtomicTask atomicTask, int timeSlotNumber, int executionTime, float price) {
        this.atomicTask = atomicTask;
        this.timeSlotNumber = timeSlotNumber;
        this.executionTime = executionTime;
        this.price = price;
    }

    public AtomicTask getAtomicTask() {
        return atomicTask;
    }

    public void setAtomicTask(AtomicTask atomicTask) {
        this.atomicTask = atomicTask;
    }

    public int getTimeSlotNumber() {
        return timeSlotNumber;
    }

    public void setTimeSlotNumber(int timeSlotNumber) {
        this.timeSlotNumber = timeSlotNumber;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "AuctionProposal{" +
                "atomicTask=" + atomicTask +
                ", timeSlotNumber=" + timeSlotNumber +
                ", executionTime=" + executionTime +
                ", price=" + price +
                '}';
    }
}
