package agents;

import bahaviours.auctioninitiator.AuctionCompletionBehaviour;
import bahaviours.auctioninitiator.AuctionInitiationBehaviour;
import jade.core.AID;
import jade.core.behaviours.SequentialBehaviour;
import entities.AtomicTask;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.OrderReader;

import java.util.ArrayList;
import java.util.List;

public class ManagerAgent extends Agent {

	private int orderCount = 1;
	private String divisionMode = "random";
	private List<AID> cachedReceivers = null;

	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			try {
				// First argument: number of orders
				orderCount = Integer.parseInt(args[0].toString());
				// Second argument: task division mode
				divisionMode = args[2].toString();
			} catch (Exception e) {
				System.err.println("Error parsing agent parameters: " + e.getMessage());
			}
		}

		// Read tasks
		OrderReader orderReader = new OrderReader(orderCount);
		orderReader.retrieveOrders();
		List<AtomicTask> atomicTaskList = orderReader.getAtomicTasksList();

		SequentialBehaviour auctionSequence = new SequentialBehaviour();
		for (AtomicTask atomicTask : atomicTaskList) {
			auctionSequence.addSubBehaviour(new AuctionInitiationBehaviour(this, atomicTask, getReceivers()));
		}
		auctionSequence.addSubBehaviour(new AuctionCompletionBehaviour(getReceivers()));
		addBehaviour(auctionSequence);
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


