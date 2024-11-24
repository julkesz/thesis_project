package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import entities.TimeSlot;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class AuctionProposalBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            try {
                AtomicTask atomicTask = (AtomicTask) msg.getContentObject();

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                AuctionProposal auctionProposal = evaluateAtomicTask(atomicTask);
                reply.setContentObject(auctionProposal);

                myAgent.send(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private AuctionProposal evaluateAtomicTask(AtomicTask atomicTask) {

        ResourceAgent resourceAgent = (ResourceAgent) myAgent;
        int timeSlotNumber = resourceAgent.calculateTimeSlotNumber(atomicTask);

        if (timeSlotNumber == -1) {
            return new AuctionProposal(atomicTask, -1, 0, Integer.MAX_VALUE);
        }

        int executionTime = calculateExecutionTime(resourceAgent, timeSlotNumber, atomicTask);
        float price = (float) executionTime;
        price = applyPriceBonuses(resourceAgent, timeSlotNumber, atomicTask, price);

        return new AuctionProposal(atomicTask, timeSlotNumber, executionTime, price);

    }

    private int calculateExecutionTime(ResourceAgent resourceAgent, int timeSlotNumber, AtomicTask atomicTask) {
        ArrayList<TimeSlot> timeSlotList = resourceAgent.getPrinterSchedule().getSchedule();

        int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / resourceAgent.getPrintingSpeed() * 60);
        int executionTime = 0;

        if(timeSlotNumber == timeSlotList.size()){
            executionTime = resourceAgent.getTotalExecutionTime() + taskExecutionTime;
            if(atomicTask.getFilament() != resourceAgent.getCurrentFilament()){
                executionTime += resourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
        }else if(timeSlotNumber == timeSlotList.size() - 1){
            TimeSlot lastTimeSlot = timeSlotList.get(timeSlotNumber);
            if (taskExecutionTime > lastTimeSlot.getExecutionTime()){
                executionTime = resourceAgent.getTotalExecutionTime() + (taskExecutionTime -lastTimeSlot.getExecutionTime());
            } else{
                executionTime = resourceAgent.getTotalExecutionTime();
            }
        }else{
            for (int i = 0; i <= timeSlotNumber; i++) {
                TimeSlot timeSlot = timeSlotList.get(i);
                if (timeSlot.isFilamentChanged()){
                    executionTime += resourceAgent.FILAMENT_REPLACEMENT_TIME;
                }
                if (i == timeSlotNumber){
                    executionTime += Math.max(timeSlot.getExecutionTime(), taskExecutionTime);
                } else{
                    executionTime += timeSlot.getExecutionTime();
                }
            }
        }
        return executionTime;

    }

    private float applyPriceBonuses(ResourceAgent resourceAgent, int timeSlotNumber, AtomicTask atomicTask, float price) {
        ArrayList<TimeSlot> timeSlotList = resourceAgent.getPrinterSchedule().getSchedule();

        if (timeSlotList.size() < timeSlotNumber + 1){
            return price;
        }
        ArrayList<AtomicTask> taskList= timeSlotList.get(timeSlotNumber).getTasks();
        float newPrice = price;

        int sameTaskCount = 0;
        int heightSum = 0;

        for (AtomicTask task : taskList) {
            heightSum += task.getHeight();
            if (task.equals(atomicTask)) {
                sameTaskCount++;
            }
        }
        if (sameTaskCount > 0){
            newPrice = newPrice - ((float) sameTaskCount/10);
        }
        if (heightSum > 0 ){
            float heightMean = (float) heightSum/taskList.size();
            newPrice = newPrice + Math.abs(heightMean - atomicTask.getHeight())/10;
        }

        // Round to two decimal places
        newPrice = (float) (Math.round(newPrice * 100.0) / 100.0);

        return newPrice;
    }
}
