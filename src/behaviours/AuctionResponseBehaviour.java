package behaviours;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.AuctionProposal;
import entities.TimeSlot;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class AuctionResponseBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            try {
                AtomicTask atomicTask = (AtomicTask) msg.getContentObject();

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                int proposal = evaluateAtomicTask(atomicTask);

                AuctionProposal auctionProposal = new AuctionProposal(atomicTask, proposal);
                reply.setContentObject(auctionProposal);
                System.out.println("Agent " + myAgent.getLocalName() + " proposed " + proposal + " for a task: " + atomicTask);

                myAgent.send(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private int evaluateAtomicTask(AtomicTask atomicTask) {

        ResourceAgent resourceAgent = (ResourceAgent) myAgent;

        int taskSize =  atomicTask.getLength() * atomicTask.getWidth();
        if (taskSize > resourceAgent.getBoardSize()) {
            return 0;
        }

        int executionTime = atomicTask.getHeight();

        if (resourceAgent.getPrinterSchedule().isEmpty()){
            if (resourceAgent.getFilament() != atomicTask.getFilament()){
                executionTime = executionTime + ResourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
        } else{
            ArrayList<TimeSlot> timeSlotList = resourceAgent.getPrinterSchedule().getSchedule();
            int lastTimeSlot = timeSlotList.size() - 1;
            int lastFilament = timeSlotList.get(lastTimeSlot).getTasks().get(0).getFilament();
            int lastTimeSlotSize = timeSlotList.get(lastTimeSlot).getTasks().size();
            AtomicTask lastAtomicTask = timeSlotList.get(lastTimeSlot).getTasks().get(lastTimeSlotSize-1);
            if (lastFilament != atomicTask.getFilament()){
                executionTime = resourceAgent.getTotalExecutionTime() + ResourceAgent.FILAMENT_REPLACEMENT_TIME + executionTime;
            } else if(resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardSize()) {
                executionTime = resourceAgent.getTotalExecutionTime() + executionTime;
            } else{
                executionTime = Math.max(executionTime, resourceAgent.getTotalExecutionTime());
            }
            if (lastAtomicTask.equals(atomicTask)){
                executionTime--;
            }
        }

        return executionTime;
    }
}
