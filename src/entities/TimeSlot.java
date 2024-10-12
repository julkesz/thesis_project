package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TimeSlot {
    private int start;
    private int stop;

    private ArrayList<AtomicTask> tasks = new ArrayList<>();

    public TimeSlot(int start, int stop) {
        this.start = start;
        this.stop = stop;
    }

    public TimeSlot() {
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public ArrayList<AtomicTask> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<AtomicTask> tasks) {
        this.tasks = tasks;
    }

    public void addTask(AtomicTask task) {
        this.tasks.add(task);
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "start=" + start +
                ", stop=" + stop +
                ", tasks=" + tasks +
                '}';
    }
}
