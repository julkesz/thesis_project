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
    private final AdvancedResourceAgent agent;

    public AuctionRequestBehaviour(AdvancedResourceAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        if (msg != null) {
            try {
                AuctionRequest auctionRequest = (AuctionRequest) msg.getContentObject();
                System.out.println(agent.getAID().getLocalName() + " received " + auctionRequest.getAtomicTasks().size() + " tasks: " + auctionRequest.getAtomicTaskIds());
                System.out.println(agent.getAID().getLocalName() + " has completion messagecount " + agent.getCompletionMessageCount());

                SequentialBehaviour auctionSequence = new SequentialBehaviour();
                for (AtomicTask atomicTask : auctionRequest.getAtomicTasks()) {
                    auctionSequence.addSubBehaviour(new AuctionInitiationBehaviour(agent, atomicTask, agent.getReceivers()));
                }
                auctionSequence.addSubBehaviour(new AuctionCompletionBehaviour(agent.getReceivers(), agent.getStartTime()));
                agent.addBehaviour(auctionSequence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}

