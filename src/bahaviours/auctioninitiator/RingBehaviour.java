package bahaviours.auctioninitiator;

import entities.messages.RingMessage;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import entities.AtomicTask;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class RingBehaviour extends Behaviour {
    
    private final AtomicTask atomicTask;
    private final List<AID> receivers;
    private boolean atomicTaskAllocated = false;
    private int step = 0;

    public RingBehaviour(AtomicTask atomicTask, List<AID> receivers) {
        this.atomicTask = atomicTask;
        this.receivers = receivers;
    }

    @Override
    public void action() {
        switch (step) {
            case 0:
                // Select a random agent from the receivers list
                AID selectedAgent = receivers.get(new Random().nextInt(receivers.size()));

                // Send CFP (Call for Proposal) message
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.addReceiver(selectedAgent);
                RingMessage ringMessage = new RingMessage(atomicTask, myAgent.getLocalName());
                try {
                    cfp.setContentObject(ringMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                myAgent.send(cfp);

                System.out.println(myAgent.getLocalName() + ": CFP sent to " + selectedAgent.getLocalName());
                step = 1;
                break;

            case 1:
                // Wait for a proposal
                ACLMessage proposal = myAgent.receive();

                if (proposal != null && proposal.getPerformative() == ACLMessage.PROPOSE) {

                    try {
                        ringMessage = (RingMessage) proposal.getContentObject();
                    } catch (UnreadableException e) {
                        throw new RuntimeException(e);
                    }

                    ACLMessage acceptProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acceptProposal.addReceiver(myAgent.getAID(ringMessage.getBestProposer()));
                    try {
                        acceptProposal.setContentObject(ringMessage.getAtomicTask());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    myAgent.send(acceptProposal);

                    System.out.println(myAgent.getLocalName() + ": ACCEPT_PROPOSAL sent to " + ringMessage.getBestProposer());

                    step = 2;
                } else {
                    block();
                }
                break;

            case 2:
                // Wait for INFORM message
                ACLMessage inform = myAgent.receive();

                if (inform != null && inform.getPerformative() == ACLMessage.INFORM) {
                    System.out.println(myAgent.getLocalName() + ": INFORM received from " + inform.getSender().getLocalName());

                    // Mark task as completed
                    atomicTaskAllocated = true;
                    System.out.println(myAgent.getLocalName() + ": Task " + atomicTask.toString() + " allocated by " + inform.getSender().getLocalName());
                } else {
                    block();
                }
                break;
        }
    }

    @Override
    public boolean done() {
        return atomicTaskAllocated;
    }
}
