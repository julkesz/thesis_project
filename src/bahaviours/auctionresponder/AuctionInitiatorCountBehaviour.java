package bahaviours.auctionresponder;

import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import agents.AdvancedResourceAgent;

public class AuctionInitiatorCountBehaviour extends SimpleBehaviour {
    private final AdvancedResourceAgent agent;
    private boolean receivedMessage = false;

    public AuctionInitiatorCountBehaviour(AdvancedResourceAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        if (msg != null && msg.getContent().matches("\\d+")) {
            int count = Integer.parseInt(msg.getContent());
            agent.setAuctionInitiatorCount(count);
            receivedMessage = true;
        } else {
            block();
        }
    }

    @Override
    public boolean done() {
        return receivedMessage;
    }
}

