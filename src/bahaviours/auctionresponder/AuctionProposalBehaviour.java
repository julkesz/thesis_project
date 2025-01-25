package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class AuctionProposalBehaviour extends CyclicBehaviour {

    public void action() {

        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));

        if (msg != null) {
            try {
                ResourceAgent resourceAgent = (ResourceAgent) myAgent;
                AtomicTask atomicTask = (AtomicTask) msg.getContentObject();

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                AuctionProposal auctionProposal = resourceAgent.evaluateAtomicTask(atomicTask);
                reply.setContentObject(auctionProposal);

                resourceAgent.send(reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }


}
