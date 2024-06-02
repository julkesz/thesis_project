package entities;


import java.io.Serializable;
import java.util.ArrayList;

public class AuctionProposal implements Serializable {
    private AtomicTask atomicTask;
    private int proposal;

    public AuctionProposal(AtomicTask atomicTask, int proposal) {
        this.atomicTask = atomicTask;
        this.proposal = proposal;
    }

    public AtomicTask getAtomicTask() {
        return atomicTask;
    }

    public int getProposal() {
        return proposal;
    }

    public void setAtomicTask(AtomicTask atomicTask) {
        this.atomicTask = atomicTask;
    }

    public void setProposal(int proposal) {
        this.proposal = proposal;
    }

    @Override
    public String toString() {
        return "AuctionProposal{" +
                "atomicTask=" + atomicTask +
                ", proposal=" + proposal +
                '}';
    }
}
