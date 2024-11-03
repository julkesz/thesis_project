package behaviours;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.AuctionProposal;
import entities.TimeSlot;
import entities.TimeSlotCalculation;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.SQLOutput;
import java.util.ArrayList;

public class AuctionResponseBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            try {
                AtomicTask atomicTask = (AtomicTask) msg.getContentObject();

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                AuctionProposal auctionProposal = evaluateAtomicTask(atomicTask);
                reply.setContentObject(auctionProposal);
                System.out.println("Agent " + myAgent.getLocalName() + " proposed " + auctionProposal);

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
        TimeSlotCalculation timeSlotCalculation = calculateTimeSlot(resourceAgent, atomicTask, false);
        int executionTime = timeSlotCalculation.getExecutionTime();

        if (executionTime == 0){
            return new AuctionProposal(atomicTask,-1, 0, Integer.MAX_VALUE);
        }else{
            float price = (float) executionTime;
            int timeSlotNumber = timeSlotCalculation.getTimeSlotNumber();
            price = applyPriceBonuses(resourceAgent, timeSlotNumber, atomicTask, price);

            return new AuctionProposal(atomicTask, timeSlotNumber, executionTime, price);

        }

    }

    private TimeSlotCalculation calculateTimeSlot(ResourceAgent resourceAgent, AtomicTask atomicTask, boolean scanAllTimeSlots) {

        int taskSize =  atomicTask.getLength() * atomicTask.getWidth();
        int boardSize = resourceAgent.getBoardWidth() * resourceAgent.getBoardLength();
        if (taskSize > boardSize) {
            return new TimeSlotCalculation();
        }
        int executionTime = Math.round((float) atomicTask.getHeight() / resourceAgent.getPrintingSpeed() * 60);

        if (resourceAgent.getPrinterSchedule().isEmpty()) {
            if (resourceAgent.getFilament() != atomicTask.getFilament()) {
                executionTime = executionTime + ResourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
            return new TimeSlotCalculation(0, executionTime);
        }

        if (!scanAllTimeSlots){
            ArrayList<TimeSlot> timeSlotList = resourceAgent.getPrinterSchedule().getSchedule();
            int lastTimeSlot = timeSlotList.size() - 1;
            int lastFilament = timeSlotList.get(lastTimeSlot).getTasks().get(0).getFilament();
            //int lastTimeSlotSize = timeSlotList.get(lastTimeSlot).getTasks().size();
            //AtomicTask lastAtomicTask = timeSlotList.get(lastTimeSlot).getTasks().get(lastTimeSlotSize-1);

            int timeSlotNumber = lastTimeSlot;
            if (lastFilament != atomicTask.getFilament()){
                executionTime = resourceAgent.getTotalExecutionTime() + ResourceAgent.FILAMENT_REPLACEMENT_TIME + executionTime;
                timeSlotNumber++;
            } else if(resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * boardSize) {
                executionTime = resourceAgent.getTotalExecutionTime() + executionTime;
                timeSlotNumber++;
            } else{
                executionTime = Math.max(executionTime, resourceAgent.getTotalExecutionTime());
            }
            return new TimeSlotCalculation(timeSlotNumber, executionTime);
        } else{
        }

        return new TimeSlotCalculation();

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
            newPrice = newPrice - (sameTaskCount/10);
        }
        if (heightSum > 0 ){
            float heightMean = (float) heightSum/taskList.size();
            newPrice = newPrice + Math.abs(heightMean - atomicTask.getHeight())/10;
        }

        return newPrice;

    }
}
