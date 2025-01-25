package bahaviours.auctioninitiator;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import entities.AtomicTask;
import entities.messages.AuctionRequest;
import agents.AdvancedResourceAgent;

import java.util.Set;
import java.util.stream.Collectors;

public class AuctionRequestBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
        AdvancedResourceAgent advancedResourceAgent = (AdvancedResourceAgent) myAgent;
        ACLMessage msg = advancedResourceAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        if (msg != null) {
            try {
                AuctionRequest auctionRequest = (AuctionRequest) msg.getContentObject();
                System.out.println(advancedResourceAgent.getAID().getLocalName() + " received " + auctionRequest.getAtomicTasks().size() + " tasks: " + auctionRequest.getAtomicTaskIds());
                System.out.println(advancedResourceAgent.getAID().getLocalName() + " has completion messagecount " + advancedResourceAgent.getCompletionMessageCount());

                SequentialBehaviour auctionSequence = new SequentialBehaviour();
                for (AtomicTask atomicTask : auctionRequest.getAtomicTasks()) {
                    auctionSequence.addSubBehaviour(new AuctionInitiationBehaviour(advancedResourceAgent, atomicTask, advancedResourceAgent.getReceivers()));
                }
                auctionSequence.addSubBehaviour(new AuctionCompletionBehaviour(advancedResourceAgent.getReceivers(), advancedResourceAgent.getStartTime()));
                advancedResourceAgent.addBehaviour(auctionSequence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}

