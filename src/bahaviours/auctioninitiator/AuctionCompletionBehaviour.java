package bahaviours.auctioninitiator;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.List;

public class AuctionCompletionBehaviour extends OneShotBehaviour {

    private List<AID> receivers;

    public AuctionCompletionBehaviour(List<AID> receivers) {
        this.receivers = receivers;
    }

    @Override
    public void action() {
        ACLMessage completionMessage = new ACLMessage(ACLMessage.CONFIRM);
        completionMessage.setContent("All auctions have been completed");

        // Add each receiver to the completion message
        for (AID receiver : receivers) {
            completionMessage.addReceiver(receiver);
        }

        // Send the completion message
        myAgent.send(completionMessage);
    }
}

