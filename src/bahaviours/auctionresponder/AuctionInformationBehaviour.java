package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.messages.AuctionCompletion;
import entities.messages.AuctionInformation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import agents.AdvancedResourceAgent;

public class AuctionInformationBehaviour extends SimpleBehaviour {
    private boolean receivedMessage = false;


    @Override
    public void action() {
        AdvancedResourceAgent advancedResourceAgent = (AdvancedResourceAgent) myAgent;
        ACLMessage msg = advancedResourceAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

        if (msg != null) {
            try {
                AuctionInformation auctionInformation = (AuctionInformation) msg.getContentObject();

                long startTime = auctionInformation.getStartTime();
                advancedResourceAgent.setStartTime(startTime);
                int auctionInitiatorCount = auctionInformation.getAuctionInitiatorCount();
                advancedResourceAgent.setAuctionInitiatorCount(auctionInitiatorCount);

                receivedMessage = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }


        if (msg != null && msg.getContent().matches("\\d+")) {
            int count = Integer.parseInt(msg.getContent());
            advancedResourceAgent.setAuctionInitiatorCount(count);
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

