package behaviours;

import agents.ResourceAgent;
import entities.AtomicTask;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

public class AuctionResponseBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            // Extract the task from the message content
            try {
                AtomicTask atomicTask = extractAtomicTaskFromMessage(msg);

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf(evaluateAtomicTask(atomicTask)));
                myAgent.send(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private AtomicTask extractAtomicTaskFromMessage(ACLMessage msg) throws Exception {
        String content = msg.getContent();
        AtomicTask atomicTask = new AtomicTask();
        return atomicTask;
    }

    private int evaluateAtomicTask(AtomicTask atomicTask) {

        ResourceAgent resourceAgent = (ResourceAgent) myAgent;

        int taskSize =  atomicTask.getLength() * atomicTask.getWidth();
        if (taskSize > resourceAgent.getBoardSize()) {
            return 0;
        }

        int executionTime = atomicTask.getExecutionTime();

        if (resourceAgent.getAtomicTaskList().isEmpty()){
            if (resourceAgent.getFilament() != atomicTask.getFilament()){
                executionTime = executionTime + ResourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
        } else{
            ArrayList<ArrayList<AtomicTask>> atomicTaskList = resourceAgent.getAtomicTaskList();
            int lastTimeSlot = atomicTaskList.size() - 1;
            int lastFilament = atomicTaskList.get(lastTimeSlot).get(0).getFilament();
            int lastTimeSlotSize = atomicTaskList.get(lastTimeSlot).size();
            AtomicTask lastTask = atomicTaskList.get(lastTimeSlot).get(lastTimeSlotSize-1);
            if (lastFilament != atomicTask.getFilament()){
                executionTime = resourceAgent.getTotalExecutionTime() + ResourceAgent.FILAMENT_REPLACEMENT_TIME + executionTime;
            } else if(resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardSize()) {
                executionTime = resourceAgent.getTotalExecutionTime() + executionTime;
            } else{
                executionTime = Math.max(executionTime, resourceAgent.getTotalExecutionTime());
            }
            if (lastTask.equals(atomicTask)){
                executionTime--;
            }
        }

        return executionTime;
    }
}
