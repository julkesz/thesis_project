package agents;

import behaviours.AuctionHandleAcceptBehaviour;
import behaviours.AuctionResponseBehaviour;
import behaviours.ContractNetInitiatorBehaviour;
import entities.AtomicTask;
import entities.AtomicTaskList;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AdvancedResourceAgent extends ResourceAgent {

    protected void setup() {
        super.setup();

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new MessageReceiverBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionResponseBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionHandleAcceptBehaviour());

        addBehaviour(parallelBehaviour);
    }

    private class MessageReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            if (msg != null) {
                try {
                    AtomicTaskList atomicTaskList = (AtomicTaskList) msg.getContentObject();
                    System.out.println("Agent " + myAgent.getAID().getLocalName() + " received: " + atomicTaskList);

                    for (AtomicTask atomicTask : atomicTaskList.getAtomicTasks()) {
                    addBehaviour(new AuctionInitiationBehaviour(atomicTask));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private class AuctionInitiationBehaviour extends OneShotBehaviour {
        private final AtomicTask atomicTask;

        public AuctionInitiationBehaviour(AtomicTask atomicTask) {
            this.atomicTask = atomicTask;
        }

        public void action() {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            try {
                cfp.setContentObject(atomicTask);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<AID> receivers = getReceivers();

            for (AID receiver : receivers) {
                cfp.addReceiver(receiver);
            }

            addBehaviour(new ContractNetInitiatorBehaviour(myAgent, cfp, receivers.size()));
        }

        private List<AID> getReceivers() {
            List<AID> receivers = new ArrayList<>();
            receivers.add(new AID("printer1", AID.ISLOCALNAME));
            receivers.add(new AID("printer2", AID.ISLOCALNAME));
            receivers.add(new AID("printer3", AID.ISLOCALNAME));
            return receivers;
        }
    }

}

