package behaviours;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.AuctionProposal;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import model.InformMessage;

import java.util.ArrayList;

public class AuctionHandleAcceptBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));

        if (msg != null) {
            try {
                AuctionProposal auctionProposal;
                int timeSlot = 0;

                try {
                    auctionProposal = (AuctionProposal) msg.getContentObject();
                    AtomicTask atomicTask = auctionProposal.getAtomicTask();
                    timeSlot = calculateTimeSlot(atomicTask);

                    ResourceAgent resourceAgent = (ResourceAgent) myAgent;
                    ArrayList<ArrayList<AtomicTask>> atomicTaskList = resourceAgent.getAtomicTaskList();

                    if(atomicTaskList.size() == timeSlot){
                        atomicTaskList.add(new ArrayList<>());
                    }

                    atomicTaskList.get(timeSlot).add(atomicTask);

                    int taskSize = atomicTask.getLength()* atomicTask.getWidth();
                    if(resourceAgent.getTotalSize()!=0 && resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardSize()) {
                        resourceAgent.setTotalSize(taskSize);
                    }else{
                        resourceAgent.setTotalSize(resourceAgent.getTotalSize() + taskSize);
                    }
                    resourceAgent.setTotalExecutionTime(auctionProposal.getProposal());
                } catch (UnreadableException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private int calculateTimeSlot(AtomicTask task) {
        ResourceAgent resourceAgent = (ResourceAgent) myAgent;

        int taskSize =  task.getLength() * task.getWidth();
        int timeSlot = 0;
        ArrayList<ArrayList<AtomicTask>> atomicTaskList = resourceAgent.getAtomicTaskList();

        if (!atomicTaskList.isEmpty()){
            int lastTimeSlot = atomicTaskList.size() - 1;
            if (atomicTaskList.get(lastTimeSlot).get(0).getFilament() != task.getFilament()
                    || resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardSize()){
                timeSlot = lastTimeSlot + 1;
            } else{
                timeSlot =  lastTimeSlot;
            }
        }
        return timeSlot;
    }
}
