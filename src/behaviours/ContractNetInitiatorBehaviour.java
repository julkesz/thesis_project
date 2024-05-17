package behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.proto.ContractNetInitiator;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Iterator;

import java.util.Vector;

public class ContractNetInitiatorBehaviour extends ContractNetInitiator {
    private int nResponders;

    private int nTasksAllocated;

    public ContractNetInitiatorBehaviour(Agent a, ACLMessage cfp, int nResponders) {
        super(a, cfp);
        this.nResponders = nResponders;
        this.nTasksAllocated = 0;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure, we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
        }
        // Evaluate proposals and create acceptances
        int bestProposal = Integer.MAX_VALUE;
        AID bestProposer = null;
        ACLMessage accept = null;

        Iterator it = (Iterator) responses.iterator();
        while (it.hasNext()) {
            ACLMessage msg = (ACLMessage) it.next();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();

                int proposal = Integer.parseInt(msg.getContent());
                if (proposal < bestProposal) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    accept = reply;
                } else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                }
            }
        }

        // Accept the proposal of the best proposer
        if (accept != null) {
            System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getLocalName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            acceptances.addElement(accept);
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
    }
}

