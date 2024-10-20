package agents;

import behaviours.AuctionHandleAcceptBehaviour;
import behaviours.AuctionResponseBehaviour;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.AtomicTask;
import entities.AtomicTaskList;
import entities.AuctionProposal;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import utils.LocalDateTimeTypeAdapter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class AdvancedResourceAgent extends ResourceAgent {
    private int auctionInitiatorCount = -1;
    private int completionMessageCount = 0;
    private List<AID> cachedReceivers = null;

    protected void setup() {
        super.setup();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("printer-service");
        sd.setName("PrinterAgentService");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new InitiatorCountMessageReceiverBehaviour());

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new MessageReceiverBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionResponseBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionHandleAcceptBehaviour());
        parallelBehaviour.addSubBehaviour(new CompletionMessageReceiverBehaviour());

        addBehaviour(parallelBehaviour);
    }

    public List<AID> getReceivers() {
        if (cachedReceivers == null) {  // Check if the receivers list is already initialized
            cachedReceivers = new ArrayList<>();  // Initialize the list
            try {
                // Create a template for the agent description we're searching for
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();

                // Set the service type to "printer-service"
                sd.setType("printer-service");
                template.addServices(sd);

                // Search the DF for agents that match the template
                DFAgentDescription[] result = DFService.search(this, template);

                // Process the results
                for (DFAgentDescription agentDesc : result) {
                    AID agentAID = agentDesc.getName();
                    cachedReceivers.add(agentAID);  // Add the AID of the agent to the cached list
                }
                System.out.println("LOOKING INTO DF");

            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }

        return cachedReceivers;
    }


    private class MessageReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            if (msg != null) {
                try {
                    AtomicTaskList atomicTaskList = (AtomicTaskList) msg.getContentObject();
                    System.out.println("Agent " + myAgent.getAID().getLocalName() + " received: " + atomicTaskList);

                    SequentialBehaviour auctionSequence = new SequentialBehaviour();
                    for (AtomicTask atomicTask : atomicTaskList.getAtomicTasks()) {
                        auctionSequence.addSubBehaviour(new AuctionInitiatorBehaviour(myAgent, atomicTask, getReceivers()));
                    }
                    auctionSequence.addSubBehaviour(new AuctionsCompletionBehaviour());
                    addBehaviour(auctionSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private class AuctionInitiatorBehaviour extends ContractNetInitiator {
        private final AtomicTask atomicTask;
        private final List<AID> receivers;

        public AuctionInitiatorBehaviour(Agent a, AtomicTask atomicTask, List<AID> receivers) {
            super(a, createCFP(atomicTask, receivers));
            this.atomicTask = atomicTask;
            this.receivers = receivers;
        }

        private static ACLMessage createCFP(AtomicTask atomicTask, List<AID> receivers) {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            try {
                cfp.setContentObject(atomicTask);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (AID receiver : receivers) {
                cfp.addReceiver(receiver);
            }
            return cfp;
        }


        protected void handlePropose(ACLMessage propose, Vector v) {
            // Handle propose
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent " + refuse.getSender().getName() + " refused");
        }

        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Agent " + failure.getSender().getName() + " failed");
            }
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < receivers.size()) {
                System.out.println("Timeout expired: missing " + (receivers.size() - responses.size()) + " responses");
            }

            int bestProposal = Integer.MAX_VALUE;
            AID bestProposer = null;
            ACLMessage accept = null;
            AtomicTask atomicTask = null;

            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);

                    AuctionProposal auctionProposal;
                    try {
                        auctionProposal = (AuctionProposal) msg.getContentObject();
                    } catch (UnreadableException ex) {
                        throw new RuntimeException(ex);
                    }

                    int proposal = auctionProposal.getProposal();
                    if (proposal < bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        atomicTask = auctionProposal.getAtomicTask();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
                System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getLocalName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                AuctionProposal acceptProposal = new AuctionProposal(atomicTask, bestProposal);
                try {
                    accept.setContentObject(acceptProposal);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("Agent " + inform.getSender().getLocalName() + " successfully added the task to their schedule");
        }
    }


    private class AuctionsCompletionBehaviour extends OneShotBehaviour {
        public void action() {
            ACLMessage completionMessage = new ACLMessage(ACLMessage.CONFIRM);
            completionMessage.setContent("All auctions have been completed");

            List<AID> receivers = getReceivers();

            for (AID receiver : receivers) {
                completionMessage.addReceiver(receiver);
            }
            send(completionMessage);
        }
    }


    private class CompletionMessageReceiverBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM));

            if (msg != null && "All auctions have been completed".equals(msg.getContent())) {
                completionMessageCount++;

                if (completionMessageCount == auctionInitiatorCount) {
                    generateJSONSchedule();
                }
            } else {
                block();
            }
        }

        private void generateJSONSchedule() {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                    .setPrettyPrinting()
                    .create();

            String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String fileName = myAgent.getLocalName() + "schedule_" + date + ".json";
            try (FileWriter fileWriter = new FileWriter("src/output/output_jsons/"+fileName)) {
                String jsonString = gson.toJson(printerSchedule);
                fileWriter.write(jsonString);
                System.out.println("JSON Schedule generated");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class InitiatorCountMessageReceiverBehaviour extends SimpleBehaviour {
        private boolean receivedMessage = false;

        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (msg != null && msg.getContent().matches("\\d+")) {
                auctionInitiatorCount = Integer.parseInt(msg.getContent());
                receivedMessage = true;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return receivedMessage;
        }
    }
}