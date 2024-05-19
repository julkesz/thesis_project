package agents;

import entities.AtomicTaskList;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import ontologies.AtomicTaskOntology;
import utils.TaskReader;

import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class SupervisorAgent extends Agent {
    protected void setup() {
        // Register the ontology and language
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(AtomicTaskOntology.getInstance());

        // Add behavior for task allocation
        addBehaviour(new TaskGroupingBehavior());
    }

    private class TaskGroupingBehavior extends OneShotBehaviour {
        public void action() {

            TaskReader taskReader = new TaskReader();
            taskReader.retrieveOrders();
            List allTasks = (List) taskReader.getAtomicTasksList();

            List taskList1 = new ArrayList();
            List taskList2 = new ArrayList();

            for(int i = 0; i < allTasks.size()/2; i++){
                taskList1.add(allTasks.get(i));
            }

            for(int i = allTasks.size()/2; i < allTasks.size(); i++){
                taskList2.add(allTasks.get(i));
            }

            // Create AtomicTaskList objects
            AtomicTaskList atomicTaskList1 = new AtomicTaskList(taskList1);
            AtomicTaskList atomicTaskList2 = new AtomicTaskList(taskList2);

            // Send the lists to different resource agents
            sendTaskList("printer1", atomicTaskList1);
            sendTaskList("printer2", atomicTaskList2);
        }

        private void sendTaskList(String agentName, AtomicTaskList taskList) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
            msg.setOntology(AtomicTaskOntology.ONTOLOGY_NAME);
            msg.setLanguage(new SLCodec().getName());

            try {
                getContentManager().fillContent(msg, taskList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            send(msg);
        }
    }
}

