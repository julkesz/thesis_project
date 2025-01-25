package bahaviours.auctionresponder;
import agents.ResourceAgent;
import agents.RingResourceAgent;
import bahaviours.auctioninitiator.AuctionCompletionBehaviour;
import entities.AtomicTask;
import entities.PrinterSchedule;
import entities.TimeSlot;
import entities.messages.AuctionProposal;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import entities.messages.RingMessage;

import java.io.IOException;
import java.util.Objects;


public class RingBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.CFP:
                    handleCfpMessage(msg);
                    break;
                case ACLMessage.ACCEPT_PROPOSAL:
                    handleAcceptProposalMessage(msg);
                    break;
                default:
                    System.out.println(myAgent.getLocalName() + ": Received unknown message type.");
            }
        } else {
            block();
        }
    }

    private void handleCfpMessage(ACLMessage msg) {
        RingResourceAgent ringResourceAgent = (RingResourceAgent) myAgent;
        
        RingMessage ringMessage;
        try {
            ringMessage = (RingMessage) msg.getContentObject();
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        if (Objects.equals(ringMessage.getFirstProposer(), ringResourceAgent.getLocalName())) {
            ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
            proposalMsg.addReceiver(ringResourceAgent.getAID(ringMessage.getSupervisor()));
            try {
                proposalMsg.setContentObject(ringMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ringResourceAgent.send(proposalMsg);
            return;
        }

        AtomicTask atomicTask = ringMessage.getAtomicTask();

        AuctionProposal auctionProposal = ringResourceAgent.evaluateAtomicTask(atomicTask);
        float price = auctionProposal.getPrice();

        if (ringMessage.getFirstProposer() == null){
            ringMessage.setFirstProposer(ringResourceAgent.getLocalName());
            ringMessage.setBestProposer(ringResourceAgent.getLocalName());
            ringMessage.setBestPrice(price);
        } else {
             if(ringMessage.getBestPrice() > price){
                 ringMessage.setBestProposer(ringResourceAgent.getLocalName());
                 ringMessage.setBestPrice(price);
             }
        }

        ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
        newMsg.addReceiver(ringResourceAgent.getAID(ringResourceAgent.getNextAgent()));
        try {
            newMsg.setContentObject(ringMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ringResourceAgent.send(newMsg);

        System.out.println(ringResourceAgent.getLocalName() + ": Task received: " + atomicTask + ". Proposal price: " + price);
    }

    private void handleAcceptProposalMessage(ACLMessage msg) {
        RingResourceAgent ringResourceAgent = (RingResourceAgent) myAgent;
            try {
                AtomicTask atomicTask;
                int timeSlotNumber = 0;

                try {
                    atomicTask = (AtomicTask) msg.getContentObject();

                    timeSlotNumber = ringResourceAgent.calculateTimeSlotNumber(atomicTask);

                    PrinterSchedule printerSchedule = ringResourceAgent.getPrinterSchedule();
                    int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / ringResourceAgent.getPrintingSpeed() * 60);

                    if (timeSlotNumber == printerSchedule.getSchedule().size()) {
                        boolean materialReplacementFlag = false;
                        if (atomicTask.getMaterial() != ringResourceAgent.getCurrentMaterial()) {
                            materialReplacementFlag = true;
                            ringResourceAgent.setCurrentMaterial(atomicTask.getMaterial());
                            ringResourceAgent.increaseTotalExecutionTime(ResourceAgent.MATERIAL_REPLACEMENT_TIME);
                        }
                        printerSchedule.addTimeSlot(materialReplacementFlag, atomicTask);
                        ringResourceAgent.increaseTotalExecutionTime(taskExecutionTime);
                    } else {
                        TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
                        int timeSlotPreviousExecutionTime = timeSlot.getExecutionTime();
                        timeSlot.addTask(atomicTask, taskExecutionTime);
                        if (taskExecutionTime > timeSlotPreviousExecutionTime) {
                            ringResourceAgent.increaseTotalExecutionTime(taskExecutionTime - timeSlotPreviousExecutionTime);
                        }
                    }
                    System.out.println(ringResourceAgent.getLocalName() + " added atomic task " + atomicTask.getAtomicTaskId() + " to timeslot " + timeSlotNumber);

                    String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(atomicTaskId);
                    ringResourceAgent.send(reply);

                } catch (UnreadableException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}