package agents;

import entities.AtomicTask;
import entities.AtomicTaskList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import utils.TaskReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupervisorAgent extends Agent {
    private int groupCount = 1;
    private String divisionMode = "random";


    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            try {
                // First argument: number of groups
                groupCount = Integer.parseInt(args[0].toString());
                // Second argument: task division mode
                if (args.length > 1) {
                    divisionMode = args[1].toString();
                }
            } catch (Exception e) {
                System.err.println("Error parsing agent parameters: " + e.getMessage());
            }
        }

        // Add the behavior to divide tasks and send to printers
        addBehaviour(new TaskGroupingBehavior());
    }

    private class TaskGroupingBehavior extends OneShotBehaviour {
        public void action() {
            // Read tasks
            TaskReader taskReader = new TaskReader();
            taskReader.retrieveOrders();
            List<AtomicTask> allAtomicTasks = taskReader.getAtomicTasksList();

            // Divide the tasks into groups
            List<List<AtomicTask>> taskGroups = divideTasksIntoGroups(allAtomicTasks, groupCount, divisionMode);


            // Search for printer agents
            List<AID> printers = getReceivers();

            // Ensure we have enough printers for the number of task groups
            if (printers.size() < taskGroups.size()) {
                System.err.println("Not enough printers available! Only " + printers.size() + " found.");
                return;
            }

            // Send each group to a printer
            for (int i = 0; i < taskGroups.size(); i++) {
                AtomicTaskList atomicTaskList = new AtomicTaskList(taskGroups.get(i));
                AID printerAID = printers.get(i);
                sendTaskList(printerAID, atomicTaskList);
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
                default:
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


        private void sendTaskList(AID printerAID, AtomicTaskList atomicTaskList) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(printerAID);

            try {
                msg.setContentObject(atomicTaskList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            send(msg);
        }
    }
}
