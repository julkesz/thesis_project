package bahaviours.supervisor;

import agents.SupervisorAgent;
import entities.AtomicTask;
import entities.messages.AuctionInformation;
import entities.messages.AuctionRequest;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import utils.OrderReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskGroupingBehaviour extends OneShotBehaviour {

    private final SupervisorAgent agent;

    public TaskGroupingBehaviour(SupervisorAgent agent) {
        this.agent = agent;
    }
    public void action() {
        agent.setStartTime(System.currentTimeMillis());
        // Read tasks
        OrderReader orderReader = new OrderReader(agent.getOrderCount());
        orderReader.retrieveOrders();
        List<AtomicTask> allAtomicTasks = orderReader.getAtomicTasksList();

        // Divide the tasks into groups
        List<List<AtomicTask>> taskGroups = divideTasksIntoGroups(allAtomicTasks, agent.getGroupCount(), agent.getDivisionMode());

        // Search for printer agents
        List<AID> printers = getReceivers();

        // Ensure we have enough printers for the number of task groups
        if (printers.size() < taskGroups.size()) {
            System.err.println("Not enough printers available! Only " + printers.size() + " found.");
            return;
        }

        // Inform printers how many auction initiators there are going to be
        for (int i = 0; i < printers.size(); i++) {
            AID printerAID = printers.get(i);
            sendAuctionInformation(printerAID);
        }

        // Send each group to a printer
        for (int i = 0; i < taskGroups.size(); i++) {
            AuctionRequest auctionRequest = new AuctionRequest(taskGroups.get(i));
            AID printerAID = printers.get(i);
            sendTaskList(printerAID, auctionRequest);
        }
    }

    private List<AID> getReceivers() {
        List<AID> receivers = new ArrayList<>();

        try {
            // Create a template for the agent description we're searching for
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();

            // Set the service type to "printer-service"
            sd.setType("printer-service");
            template.addServices(sd);

            // Search the DF for agents that match the template
            DFAgentDescription[] result = DFService.search(myAgent, template);

            // Process the results
            for (DFAgentDescription agentDesc : result) {
                AID agentAID = agentDesc.getName();
                receivers.add(agentAID);  // Add the AID of the agent to the list
            }

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Print the receivers for debugging purposes
        System.out.println("RECEIVERS:  " + receivers);

        return receivers;
    }


    private List<List<AtomicTask>> divideTasksIntoGroups(List<AtomicTask> allAtomicTasks, int groupCount, String divisionMode) {
        List<List<AtomicTask>> groups = new ArrayList<>();

        switch(divisionMode) {
            case "random":
                Collections.shuffle(allAtomicTasks);
                break;
            case "size":
                allAtomicTasks.sort((task1, task2) -> {
                    int size1 = task1.getLength() * task1.getWidth();
                    int size2 = task2.getLength() * task2.getWidth();

                    if (size1 != size2) {
                        return Integer.compare(size2, size1);
                    }
                    return Integer.compare(task1.getOrderId(), task2.getOrderId());
                });
                break;
            case "deadline":
                break;
        }

        // Initialize empty lists for each group
        for (int i = 0; i < groupCount; i++) {
            groups.add(new ArrayList<>());
        }

        // Distribute tasks across the groups
        for (int i = 0; i < allAtomicTasks.size(); i++) {
            groups.get(i % groupCount).add(allAtomicTasks.get(i));
        }

        return groups;
    }


    private void sendTaskList(AID printerAID, AuctionRequest auctionRequest) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(printerAID);

        try {
            msg.setContentObject(auctionRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        agent.send(msg);
    }

    private void sendAuctionInformation(AID printerAID) {

        AuctionInformation auctionInformation = new AuctionInformation(agent.getStartTime(), agent.getGroupCount());
        ACLMessage informationMessage = new ACLMessage(ACLMessage.INFORM);
        try {
            informationMessage.setContentObject(auctionInformation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        informationMessage.addReceiver(printerAID);
        agent.send(informationMessage);

    }

}
