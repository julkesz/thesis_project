package agents;

import bahaviours.auctionresponder.AuctionAcceptanceBehaviour;
import bahaviours.auctionresponder.AuctionProposalBehaviour;
import bahaviours.auctionresponder.AuctionCompletionBehaviour;
import bahaviours.auctioninitiator.AuctionRequestBehaviour;
import bahaviours.auctionresponder.AuctionInformationBehaviour;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.*;


public class AdvancedResourceAgent extends ResourceAgent {
    private List<AID> cachedReceivers = null;
    private String parallelAcceptanceMode; //overbook, startauction


    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length == 7) {
            try {
                //Seventh argument:
                parallelAcceptanceMode = args[6].toString();
            } catch (Exception e) {
                System.err.println("Error parsing agent parameters: " + e.getMessage());
            }
        }

        addBehaviour(new AuctionInformationBehaviour());

        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();
        parallelBehaviour.addSubBehaviour(new AuctionRequestBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionProposalBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionAcceptanceBehaviour());
        parallelBehaviour.addSubBehaviour(new AuctionCompletionBehaviour());

        addBehaviour(parallelBehaviour);
    }

    public String getParallelAcceptanceMode() {
        return parallelAcceptanceMode;
    }

    public List<AID> getReceivers() {
        if (cachedReceivers == null) {
            cachedReceivers = new ArrayList<>();
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
                    cachedReceivers.add(agentAID);
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        return cachedReceivers;
    }


}