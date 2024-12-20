package entities.messages;


import entities.AtomicTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AuctionRequest implements Serializable {
    private List<AtomicTask> atomicTasks;
    private List<Integer> atomicTaskIds;

    public AuctionRequest(List<AtomicTask> atomicTasks) {

        this.atomicTasks = atomicTasks;
        this.atomicTaskIds = new ArrayList<>();
        for (AtomicTask atomicTask : atomicTasks) {
            this.atomicTaskIds.add(atomicTask.getAtomicTaskId());
        }
    }

    public List<AtomicTask> getAtomicTasks() {
        return atomicTasks;
    }

    public List<Integer> getAtomicTaskIds() {
        return atomicTaskIds;
    }

    @Override
    public String toString() {
        return "AtomicTaskList{" +
                "atomicTasks=" + atomicTasks +
                '}';
    }
}

