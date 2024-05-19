package entities;

// TaskList.java
import jade.content.AgentAction;
import jade.util.leap.List;

public class AtomicTaskList implements AgentAction {
    private List atomicTasks;

    public AtomicTaskList(List atomicTasks) {
        this.atomicTasks = atomicTasks;
    }

    public List getAtomicTasks() {
        return atomicTasks;
    }

    public void setAtomicTasks(List atomicTasks) {
        this.atomicTasks = atomicTasks;
    }
}

