package model;

import java.io.Serializable;
import entities.AtomicTask;

public class InformMessage implements Serializable {
    private int timeSlot;
    private AtomicTask task;

    public InformMessage() {
    }

    public InformMessage(int timeSlot, AtomicTask task) {
        this.timeSlot = timeSlot;
        this.task = task;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(int timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AtomicTask getTask() {
        return task;
    }

    public void setTask(AtomicTask task) {
        this.task = task;
    }
}
