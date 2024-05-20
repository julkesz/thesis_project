package behaviours;

import agents.ResourceAgent;
import entities.AtomicTask;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import model.InformMessage;

import java.io.IOException;
import java.util.ArrayList;

public class AuctionResponseBehaviour extends ContractNetResponder {

    public AuctionResponseBehaviour(ResourceAgent agent, MessageTemplate template) {
        super(agent, template);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        ACLMessage reply = cfp.createReply();
        try {
            AtomicTask atomicTask = (AtomicTask) cfp.getContentObject();
            int proposal = evaluateAtomicTask(atomicTask);

            if (proposal > 0) {
                reply.setPerformative(ACLMessage.PROPOSE);
                reply.setContent(String.valueOf(proposal));
                System.out.println("Agent " + myAgent.getLocalName() + " proposed " + proposal + " for a task: " + atomicTask);
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Not interested");
                System.out.println("Agent " + myAgent.getLocalName() + " refused the task: " + atomicTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("Error processing the request");
        }

        return reply;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        AtomicTask atomicTask;
        int timeSlot = 0;

        try {
            atomicTask = (AtomicTask) cfp.getContentObject();
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
            resourceAgent.setTotalExecutionTime(Integer.parseInt(propose.getContent()));
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        InformMessage messageContent = new InformMessage(timeSlot, atomicTask);

        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        try {
            inform.setContentObject(messageContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return inform;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
    }

    private int evaluateAtomicTask(AtomicTask atomicTask) {
        ResourceAgent resourceAgent = (ResourceAgent) myAgent;

        int taskSize = atomicTask.getLength() * atomicTask.getWidth();
        if (taskSize > resourceAgent.getBoardSize()) {
            return 0;
        }

        int executionTime = atomicTask.getExecutionTime();

        if (resourceAgent.getAtomicTaskList().isEmpty()) {
            if (resourceAgent.getFilament() != atomicTask.getFilament()) {
                executionTime = executionTime + ResourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
        } else {
            ArrayList<ArrayList<AtomicTask>> atomicTaskList = resourceAgent.getAtomicTaskList();
            int lastTimeSlot = atomicTaskList.size() - 1;
            int lastFilament = atomicTaskList.get(lastTimeSlot).get(0).getFilament();
            int lastTimeSlotSize = atomicTaskList.get(lastTimeSlot).size();
            AtomicTask lastTask = atomicTaskList.get(lastTimeSlot).get(lastTimeSlotSize - 1);
            if (lastFilament != atomicTask.getFilament()) {
                executionTime = resourceAgent.getTotalExecutionTime() + ResourceAgent.FILAMENT_REPLACEMENT_TIME + executionTime;
            } else if (resourceAgent.getTotalSize() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardSize()) {
                executionTime = resourceAgent.getTotalExecutionTime() + executionTime;
            } else {
                executionTime = Math.max(executionTime, resourceAgent.getTotalExecutionTime());
            }
            if (lastTask.equals(atomicTask)) {
                executionTime--;
            }
        }

        return executionTime;
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