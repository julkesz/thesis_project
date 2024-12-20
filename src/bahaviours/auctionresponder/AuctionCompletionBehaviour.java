package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.messages.AuctionCompletion;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AuctionCompletionBehaviour extends CyclicBehaviour {
    private final ResourceAgent agent;


    public AuctionCompletionBehaviour(ResourceAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));

        if (msg != null) {
            try {
                AuctionCompletion auctionCompletion = (AuctionCompletion) msg.getContentObject();

                agent.increaseCompletionMessageCount();

                if (agent.getCompletionMessageCount()== agent.getAuctionInitiatorCount()) {
                    agent.setElapsedTime(auctionCompletion.getElapsedTime());
                    agent.completePrinterSchedule();
                    agent.generateJSONSchedule();
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

