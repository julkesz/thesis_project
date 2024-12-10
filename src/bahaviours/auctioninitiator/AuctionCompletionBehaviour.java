package bahaviours.auctioninitiator;

import entities.messages.AuctionCompletion;
import entities.messages.AuctionProposal;
import entities.messages.AuctionRequest;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.List;

public class AuctionCompletionBehaviour extends OneShotBehaviour {

    private long startTime;
    private List<AID> receivers;

    public AuctionCompletionBehaviour(List<AID> receivers, long startTime) {
        this.receivers = receivers;
        this.startTime = startTime;
    }

    @Override
    public void action() {

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        AuctionCompletion auctionCompletion = new AuctionCompletion(elapsedTime);
        ACLMessage completionMessage = new ACLMessage(ACLMessage.CONFIRM);

        try {
            completionMessage.setContentObject(auctionCompletion);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (AID receiver : receivers) {
            completionMessage.addReceiver(receiver);
        }

        System.out.println("AUCTION COMPLETION IS SENT!!!!!");

        myAgent.send(completionMessage);
    }
}

