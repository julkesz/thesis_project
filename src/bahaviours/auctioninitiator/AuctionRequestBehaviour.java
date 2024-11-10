package bahaviours.auctioninitiator;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import entities.AtomicTask;
import entities.AtomicTaskList;
import agents.AdvancedResourceAgent;

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
                AtomicTaskList atomicTaskList = (AtomicTaskList) msg.getContentObject();
                System.out.println(agent.getAID().getLocalName() + " received " + atomicTaskList.getAtomicTasks().size() + " tasks: " + atomicTaskList.getAtomicTaskIds());

                SequentialBehaviour auctionSequence = new SequentialBehaviour();
                for (AtomicTask atomicTask : atomicTaskList.getAtomicTasks()) {
                    auctionSequence.addSubBehaviour(new AuctionInitiationBehaviour(agent, atomicTask, agent.getReceivers()));
                }
                auctionSequence.addSubBehaviour(new AuctionCompletionBehaviour(agent.getReceivers()));
                agent.addBehaviour(auctionSequence);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}

