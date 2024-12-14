package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class AuctionProposalBehaviour extends CyclicBehaviour {
    private final ResourceAgent agent;

    public AuctionProposalBehaviour(ResourceAgent agent) {
        this.agent = agent;
    }

    public void action() {
        ACLMessage msg = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            try {
                AtomicTask atomicTask = (AtomicTask) msg.getContentObject();

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                AuctionProposal auctionProposal = agent.evaluateAtomicTask(atomicTask);
                reply.setContentObject(auctionProposal);

                myAgent.send(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }


}
