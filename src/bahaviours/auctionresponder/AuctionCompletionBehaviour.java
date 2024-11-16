package bahaviours.auctionresponder;

import agents.ResourceAgent;
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

        if (msg != null && "All auctions have been completed".equals(msg.getContent())) {
            agent.increaseCompletionMessageCount();

            // Check if all auctions have completed
            if (agent.getCompletionMessageCount()== agent.getAuctionInitiatorCount()) {
                agent.completePrinterSchedule();
                agent.generateJSONSchedule();
                System.out.println("All auctions completed; JSON schedule generated.");
            }
        } else {
            block();
        }
    }
}

