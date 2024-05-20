package entities;


import java.io.Serializable;
import java.util.ArrayList;

public class AtomicTaskList implements Serializable {
    private ArrayList<AtomicTask> atomicTasks;

    public AtomicTaskList(ArrayList<AtomicTask> atomicTasks) {
        this.atomicTasks = atomicTasks;
    }

    public ArrayList<AtomicTask> getAtomicTasks() {
        return atomicTasks;
    }

    public void setAtomicTasks(ArrayList<AtomicTask> atomicTasks) {
        this.atomicTasks = atomicTasks;
    }

    @Override
    public String toString() {
        return "AtomicTaskList{" +
                "atomicTasks=" + atomicTasks +
                '}';
    }
}

