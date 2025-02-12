package agents;

import bahaviours.auctioninitiator.AuctionCompletionBehaviour;
import bahaviours.auctioninitiator.RingBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import entities.AtomicTask;

import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.OrderReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RingSupervisorAgent extends Agent {
    private long startTime;
    private String orderFileName;
    private String divisionMode = "random";
    private List<AID> cachedReceivers = null;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            try {
                orderFileName = args[0].toString();
                divisionMode = args[1].toString();
            } catch (Exception e) {
                System.err.println("Error parsing agent parameters: " + e.getMessage());
            }
        }

        startTime = System.currentTimeMillis();
        // Read tasks
        OrderReader orderReader = new OrderReader(orderFileName);
        orderReader.retrieveOrders();
        List<AtomicTask> atomicTaskList = sortAtomicTasks(orderReader.getAtomicTasksList());

        SequentialBehaviour auctionSequence = new SequentialBehaviour();
        for (AtomicTask atomicTask : atomicTaskList) {
            auctionSequence.addSubBehaviour(new RingBehaviour(atomicTask, getReceivers()));
        }
        auctionSequence.addSubBehaviour(new AuctionCompletionBehaviour(getReceivers(), startTime));
        addBehaviour(auctionSequence);
    }

    private List<AtomicTask> sortAtomicTasks(List<AtomicTask> atomicTasksList) {

        switch(divisionMode) {
            case "random":
                Collections.shuffle(atomicTasksList);
                break;
            case "size":
                atomicTasksList.sort((task1, task2) -> {
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
        return atomicTasksList;
    }

    public List<AID> getReceivers() {
        if (cachedReceivers == null) {
            cachedReceivers = new ArrayList<>();
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();

                sd.setType("printer-service");
                template.addServices(sd);

                DFAgentDescription[] result = DFService.search(this, template);

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


