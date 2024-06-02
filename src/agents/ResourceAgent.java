package agents;

import entities.AtomicTask;
import jade.core.Agent;

import java.util.ArrayList;

public class ResourceAgent extends Agent {

    protected int boardSize;
    protected int filament;
    protected ArrayList<ArrayList<AtomicTask>> atomicTaskList;
    protected int totalExecutionTime;
    protected int totalSize;
    public static final int FILAMENT_REPLACEMENT_TIME = 20;
    public static final double BOARD_HEURISTICS = 0.8;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            boardSize = Integer.parseInt((String) args[0]);
            filament = Integer.parseInt((String) args[1]);
            System.out.println("Agent " + getLocalName() + " has board of size " + boardSize + " and filament number " + filament + ".");
        } else {
            System.out.println("No arguments provided.");
        }

        atomicTaskList = new ArrayList();
        totalExecutionTime = 0;
        totalSize = 0;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getFilament() {
        return filament;
    }

    public ArrayList<ArrayList<AtomicTask>> getAtomicTaskList() {
        return atomicTaskList;
    }

    public int getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public void setFilament(int filament) {
        this.filament = filament;
    }

    public void setAtomicTaskList(ArrayList<ArrayList<AtomicTask>> atomicTaskList) {
        this.atomicTaskList = atomicTaskList;
    }

    public void setTotalExecutionTime(int totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
