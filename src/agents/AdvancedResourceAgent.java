package agents;

import behaviours.AuctionResponseBehaviour;
import behaviours.ContractNetInitiatorBehaviour;
import entities.AtomicTask;
import entities.AtomicTaskList;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;

import ontologies.AtomicTaskOntology;

import java.util.List;
import java.util.ArrayList;

public class AdvancedResourceAgent extends ResourceAgent {

    protected void setup() {
        super.setup();

        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(AtomicTaskOntology.getInstance());

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new MessageReceiverBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionResponseBehaviour());

        addBehaviour(parallelBehaviour);
    }

    private class MessageReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();

            if (msg != null) {
                try {
                    ContentElement content = getContentManager().extractContent(msg);

                    if (content instanceof AtomicTaskList) {
                        AtomicTaskList atomicTaskList = (AtomicTaskList) content;
                        //for (AtomicTask atomicTask : atomicTaskList.getAtomicTasks()) {
                        //    // Initiate auction for each task
                        //addBehaviour(new AuctionInitiationBehaviour(atomicTask));
                        //}
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
        private AtomicTask atomicTask;

        public AuctionInitiationBehaviour(AtomicTask atomicTask) {
            this.atomicTask = atomicTask;
        }

        public void action() {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setContent(atomicTask.toString());
            cfp.setOntology(AtomicTaskOntology.ONTOLOGY_NAME);
            cfp.setLanguage(new SLCodec().getName());

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

