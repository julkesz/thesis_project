package bahaviours.auctioninitiator;

import agents.AdvancedResourceAgent;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import entities.AtomicTask;
import entities.messages.AuctionProposal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class AuctionInitiationBehaviour extends ContractNetInitiator {
    private final Agent agent;
    private final AtomicTask atomicTask;
    private final List<AID> receivers;

    public AuctionInitiationBehaviour(Agent agent, AtomicTask atomicTask, List<AID> receivers) {
        super(agent, createCFP(agent, atomicTask, receivers));
        this.agent = agent;
        this.atomicTask = atomicTask;
        this.receivers = receivers;
    }

    public AuctionInitiationBehaviour(Agent agent, AtomicTask atomicTask, List<AID> receivers, AID originalAuctionInitiator, String originalConversationId) {
        super(agent, createCFP(agent, atomicTask, receivers, originalConversationId));
        this.agent = agent;
        this.atomicTask = atomicTask;
        this.receivers = receivers;
    }

    private static ACLMessage createCFP(Agent agent, AtomicTask atomicTask, List<AID> receivers) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        try {
            cfp.setContentObject(atomicTask);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (AID receiver : receivers) {
            cfp.addReceiver(receiver);
        }

        System.out.println(agent.getLocalName() + " started auction for atomic task " + atomicTask.getAtomicTaskId() +
                " with conversation ID: " + cfp.getConversationId());
        return cfp;
    }

    private static ACLMessage createCFP(Agent agent, AtomicTask atomicTask, List<AID> receivers, String originalConversationId) {
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

        try {
            cfp.setContentObject(atomicTask);
            cfp.setConversationId(originalConversationId); // Pass the original conversation ID
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (AID receiver : receivers) {
            cfp.addReceiver(receiver);
        }

        System.out.println(agent.getLocalName() + " started auction for atomic task " + atomicTask.getAtomicTaskId() +
                " with conversation ID: " + originalConversationId);
        return cfp;
    }


    @Override
    protected void handlePropose(ACLMessage propose, Vector v) {
        // Handle propose
    }

    @Override
    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    @Override
    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(agent.getAMS())) {
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < receivers.size()) {
            System.out.println("Timeout expired: missing " + (receivers.size() - responses.size()) + " responses");
        }

        float bestProposal = Float.MAX_VALUE;
        AID bestProposer = null;
        ACLMessage accept = null;
        int timeSlotNumber = -1;
        int executionTime = 0;
        AtomicTask task = null;

        StringBuilder proposalsSummary = new StringBuilder();
        if (!responses.isEmpty()) {
            ACLMessage firstMsg = (ACLMessage) responses.get(0);
            try {
                AuctionProposal firstProposal = (AuctionProposal) firstMsg.getContentObject();
                proposalsSummary.append("Proposals for atomic task ").append(firstProposal.getAtomicTask().getAtomicTaskId()).append(":\n");
            } catch (UnreadableException ex) {
                ex.printStackTrace();
            }
        }

        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            String conversationId = msg.getConversationId();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);

                AuctionProposal auctionProposal;
                try {
                    auctionProposal = (AuctionProposal) msg.getContentObject();
                } catch (UnreadableException ex) {
                    throw new RuntimeException(ex);
                }

                float proposal = auctionProposal.getPrice();
                String proposerName = msg.getSender().getLocalName();

                proposalsSummary.append(proposerName).append(": timeslot ").append(auctionProposal.getTimeSlotNumber())
                        .append(", time ").append(auctionProposal.getExecutionTime()).append(" minutes, price ")
                        .append(proposal).append("\n");

                if (proposal < bestProposal) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    task = auctionProposal.getAtomicTask();
                    timeSlotNumber = auctionProposal.getTimeSlotNumber();
                    executionTime = auctionProposal.getExecutionTime();
                    accept = reply;
                }
            }
        }

        System.out.println(proposalsSummary);

        if (accept != null) {
            System.out.println("Accepting proposal " + bestProposal + " for atomic task " + task.getAtomicTaskId()
                    + " from " + bestProposer.getLocalName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            AuctionProposal acceptProposal = new AuctionProposal(task, timeSlotNumber, executionTime, bestProposal);
            try {
                accept.setContentObject(acceptProposal);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


    @Override
    protected void handleInform(ACLMessage inform) {
        System.out.println(agent.getLocalName() +" received INFORM from " + inform.getSender().getLocalName() +
                " about atomicTask " + inform.getContent());
    }


}

