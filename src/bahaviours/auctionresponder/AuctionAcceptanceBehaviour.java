package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.AuctionProposal;
import entities.PrinterSchedule;
import entities.TimeSlot;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.NoSuchElementException;

public class AuctionAcceptanceBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));

        if (msg != null) {
            try {
                AuctionProposal auctionProposal;
                int timeSlotNumber = 0;

                try {
                    auctionProposal = (AuctionProposal) msg.getContentObject();
                    AtomicTask atomicTask = auctionProposal.getAtomicTask();
                    timeSlotNumber = calculateTimeSlot(atomicTask);

                    ResourceAgent resourceAgent = (ResourceAgent) myAgent;
                    PrinterSchedule printerSchedule = resourceAgent.getPrinterSchedule();

                    if(printerSchedule.getSchedule().size() == timeSlotNumber && atomicTask.getFilament() != resourceAgent.getFilament()){
                        printerSchedule.addTimeSlot(resourceAgent.getTotalExecutionTime() + resourceAgent.FILAMENT_REPLACEMENT_TIME);
                    }else if(printerSchedule.getSchedule().size() == timeSlotNumber){
                        printerSchedule.addTimeSlot(resourceAgent.getTotalExecutionTime());
                    }

                    TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
                    timeSlot.addTask(atomicTask);

                    int maxHeight = timeSlot.getTasks().stream()
                            .mapToInt(AtomicTask::getHeight).max().orElseThrow(NoSuchElementException::new);

                    int executionTime = Math.round((float) maxHeight / resourceAgent.getPrintingSpeed() * 60);

                    int start = printerSchedule.getSchedule().get(timeSlotNumber).getStart();
                    int stop = start + executionTime;
                    printerSchedule.getSchedule().get(timeSlotNumber).setStop(stop);

                    resourceAgent.setTotalExecutionTime(stop);
                    resourceAgent.setFilament(atomicTask.getFilament());

                    System.out.println(resourceAgent.getLocalName() + " added atomic task " + atomicTask.getAtomicTaskId() + " to timeslot " + timeSlotNumber);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    myAgent.send(reply);
                } catch (UnreadableException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }

    private int calculateTimeSlot(AtomicTask task) {
        ResourceAgent resourceAgent = (ResourceAgent) myAgent;

        int taskSize =  task.getLength() * task.getWidth();
        int timeSlot = 0;
        PrinterSchedule printerSchedule = resourceAgent.getPrinterSchedule();

        if (!printerSchedule.isEmpty()){
            int lastTimeSlot = printerSchedule.getSchedule().size() - 1;

            if (printerSchedule.getSchedule().get(lastTimeSlot).getTasks().get(0).getFilament() != task.getFilament()
                    || resourceAgent.getLastTimeSlotOccupancy() + taskSize > ResourceAgent.BOARD_HEURISTICS * resourceAgent.getBoardWidth() * resourceAgent.getBoardLength()){
                timeSlot = lastTimeSlot + 1;
            } else{
                timeSlot =  lastTimeSlot;
            }
        }
        return timeSlot;
    }
}
