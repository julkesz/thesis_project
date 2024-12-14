package entities.messages;


import entities.AtomicTask;

import java.io.Serializable;


public class RingMessage implements Serializable {
    private String supervisor;
    private AtomicTask atomicTask;
    private float bestPrice = Float.MAX_VALUE;
    private String bestProposer = null;
    private String firstProposer = null;


    public RingMessage(AtomicTask atomicTask, String supervisor) {
        this.supervisor = supervisor;
        this.atomicTask = atomicTask;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public AtomicTask getAtomicTask() {
        return atomicTask;
    }

    public float getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(float bestPrice) {
        this.bestPrice = bestPrice;
    }

    public String getBestProposer() {
        return bestProposer;
    }

    public void setBestProposer(String bestProposer) {
        this.bestProposer = bestProposer;
    }

    public String getFirstProposer() {
        return firstProposer;
    }

    public void setFirstProposer(String firstProposer) {
        this.firstProposer = firstProposer;
    }
}
