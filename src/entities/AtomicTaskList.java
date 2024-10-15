package entities;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AtomicTaskList implements Serializable {
    private List<AtomicTask> atomicTasks;

    public AtomicTaskList(List<AtomicTask> atomicTasks) {
        this.atomicTasks = atomicTasks;
    }

    public List<AtomicTask> getAtomicTasks() {
        return atomicTasks;
    }

    public void setAtomicTasks(List<AtomicTask> atomicTasks) {
        this.atomicTasks = atomicTasks;
    }

    @Override
    public String toString() {
        return "AtomicTaskList{" +
                "atomicTasks=" + atomicTasks +
                '}';
    }
}

