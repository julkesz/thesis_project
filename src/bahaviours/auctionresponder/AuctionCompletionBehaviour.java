package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.messages.AuctionCompletion;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AuctionCompletionBehaviour extends CyclicBehaviour {


    @Override
    public void action() {

        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));

        if (msg != null) {
            try {
                AuctionCompletion auctionCompletion = (AuctionCompletion) msg.getContentObject();
                ResourceAgent resourceAgent = (ResourceAgent) myAgent;

                resourceAgent.increaseCompletionMessageCount();

                if (resourceAgent.getCompletionMessageCount() == resourceAgent.getAuctionInitiatorCount()) {
                    resourceAgent.setElapsedTime(auctionCompletion.getElapsedTime());
                    resourceAgent.completePrinterSchedule();
                    resourceAgent.generateJSONSchedule();
                    System.out.println("All auctions completed; JSON schedule generated.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }

    }
}

