package bahaviours.auctionresponder;

import entities.messages.AuctionCompletion;
import entities.messages.AuctionInformation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import agents.AdvancedResourceAgent;

public class AuctionInformationBehaviour extends SimpleBehaviour {
    private final AdvancedResourceAgent agent;
    private boolean receivedMessage = false;

    public AuctionInformationBehaviour(AdvancedResourceAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        if (msg != null) {
            try {
                AuctionInformation auctionInformation = (AuctionInformation) msg.getContentObject();
                agent.increaseCompletionMessageCount();

                long startTime = auctionInformation.getStartTime();
                agent.setStartTime(startTime);
                int auctionInitiatorCount = auctionInformation.getAuctionInitiatorCount();
                agent.setAuctionInitiatorCount(auctionInitiatorCount);

                receivedMessage = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }








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

