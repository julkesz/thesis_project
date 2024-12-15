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
    private final RingResourceAgent agent;

    public RingBehaviour(RingResourceAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();
        if (msg != null) {
            switch (msg.getPerformative()) {
                case ACLMessage.CFP:
                    handleCfpMessage(msg);
                    break;
                case ACLMessage.ACCEPT_PROPOSAL:
                    handleAcceptProposalMessage(msg);
                    break;
                default:
                    System.out.println(agent.getLocalName() + ": Received unknown message type.");
            }
        } else {
            block();
        }
    }

    private void handleCfpMessage(ACLMessage msg) {
        RingMessage ringMessage;
        try {
            ringMessage = (RingMessage) msg.getContentObject();
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }

        if (Objects.equals(ringMessage.getFirstProposer(), agent.getLocalName())) {
            ACLMessage proposalMsg = new ACLMessage(ACLMessage.PROPOSE);
            proposalMsg.addReceiver(agent.getAID(ringMessage.getSupervisor()));
            try {
                proposalMsg.setContentObject(ringMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            agent.send(proposalMsg);
            return;
        }

        AtomicTask atomicTask = ringMessage.getAtomicTask();

        AuctionProposal auctionProposal = agent.evaluateAtomicTask(atomicTask);
        float price = auctionProposal.getPrice();

        if (ringMessage.getFirstProposer() == null){
            ringMessage.setFirstProposer(agent.getLocalName());
            ringMessage.setBestProposer(agent.getLocalName());
            ringMessage.setBestPrice(price);
        } else {
             if(ringMessage.getBestPrice() > price){
                 ringMessage.setBestProposer(agent.getLocalName());
                 ringMessage.setBestPrice(price);
             }
        }

        ACLMessage newMsg = new ACLMessage(ACLMessage.CFP);
        newMsg.addReceiver(agent.getAID(agent.getNextAgent()));
        try {
            newMsg.setContentObject(ringMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        agent.send(newMsg);

        System.out.println(agent.getLocalName() + ": Task received: " + atomicTask + ". Proposal price: " + price);
    }

    private void handleAcceptProposalMessage(ACLMessage msg) {
            try {
                AtomicTask atomicTask;
                int timeSlotNumber = 0;

                try {
                    atomicTask = (AtomicTask) msg.getContentObject();

                    timeSlotNumber = agent.calculateTimeSlotNumber(atomicTask);

                    PrinterSchedule printerSchedule = agent.getPrinterSchedule();
                    int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / agent.getPrintingSpeed() * 60);

                    if (timeSlotNumber == printerSchedule.getSchedule().size()) {
                        boolean filamentReplacementFlag = false;
                        if (atomicTask.getFilament() != agent.getCurrentFilament()) {
                            filamentReplacementFlag = true;
                            agent.setCurrentFilament(atomicTask.getFilament());
                            agent.increaseTotalExecutionTime(ResourceAgent.FILAMENT_REPLACEMENT_TIME);
                        }
                        printerSchedule.addTimeSlot(filamentReplacementFlag, atomicTask);
                        agent.increaseTotalExecutionTime(taskExecutionTime);
                    } else {
                        TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
                        int timeSlotPreviousExecutionTime = timeSlot.getExecutionTime();
                        timeSlot.addTask(atomicTask, taskExecutionTime);
                        if (taskExecutionTime > timeSlotPreviousExecutionTime) {
                            agent.increaseTotalExecutionTime(taskExecutionTime - timeSlotPreviousExecutionTime);
                        }
                    }
                    System.out.println(agent.getLocalName() + " added atomic task " + atomicTask.getAtomicTaskId() + " to timeslot " + timeSlotNumber);

                    String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(atomicTaskId);
                    agent.send(reply);

                } catch (UnreadableException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

}