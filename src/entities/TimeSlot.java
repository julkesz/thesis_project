package entities;

import java.util.ArrayList;

public class TimeSlot {
    private boolean filamentReplacementFlag;
    private int executionTime = 0;
    private int start;
    private int stop;

    private ArrayList<AtomicTask> tasks;


    public TimeSlot(boolean filamentReplacementFlag, AtomicTask atomicTask, int taskExecutionTime) {
        this.filamentReplacementFlag = filamentReplacementFlag;
        this.tasks = new ArrayList<>();
        this.tasks.add(atomicTask);
        this.executionTime = taskExecutionTime;
    }

    public int getFilament() {
        return tasks.get(0).getFilament();
    }

    public int getExecutionTime() {
        return executionTime;
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

    public void addTask(AtomicTask task, int taskExecutionTime) {
        this.tasks.add(task);
        if (taskExecutionTime > executionTime){
            this.executionTime = taskExecutionTime;
        }
    }

    public int getOccupancy() {
        int occupancy = 0;
        for (AtomicTask task : tasks){
            occupancy += task.getWidth() * task.getLength();
        }
        return occupancy;
    }

    public boolean isFilamentChanged() {
        return filamentReplacementFlag;
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
