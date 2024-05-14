package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TimeSlot {
    private LocalDateTime start;
    private LocalDateTime stop;

    private ArrayList<AtomicTask> tasks;

    public TimeSlot(LocalDateTime start, LocalDateTime stop) {
        this.start = start;
        this.stop = stop;
        this.tasks = new ArrayList<>();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getStop() {
        return stop;
    }

    public void setStop(LocalDateTime stop) {
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
