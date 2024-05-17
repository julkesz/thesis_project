package entities;

// TaskList.java
import jade.content.AgentAction;
import java.util.ArrayList;
import java.util.List;

public class AtomicTaskList implements AgentAction {
    private List<AtomicTask> atomicTasks = new ArrayList<>();

    public List<AtomicTask> getAtomicTasks() {
        return atomicTasks;
    }

    public void setAtomicTasks(List<AtomicTask> atomicTasks) {
        this.atomicTasks = atomicTasks;
    }
}

