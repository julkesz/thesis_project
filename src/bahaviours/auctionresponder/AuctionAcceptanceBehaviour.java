package bahaviours.auctionresponder;

import agents.ResourceAgent;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import entities.PrinterSchedule;
import entities.TimeSlot;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

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
                    ResourceAgent resourceAgent = (ResourceAgent) myAgent;
                    timeSlotNumber = resourceAgent.calculateTimeSlotNumber(atomicTask);

                    PrinterSchedule printerSchedule = resourceAgent.getPrinterSchedule();
                    int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / resourceAgent.getPrintingSpeed() * 60);

                    if(timeSlotNumber == printerSchedule.getSchedule().size()){
                        boolean filamentReplacementFlag = false;
                        if(atomicTask.getFilament() != resourceAgent.getCurrentFilament()){
                            filamentReplacementFlag = true;
                            resourceAgent.setCurrentFilament(atomicTask.getFilament());
                            resourceAgent.increaseTotalExecutionTime(ResourceAgent.FILAMENT_REPLACEMENT_TIME);
                        }
                        printerSchedule.addTimeSlot(filamentReplacementFlag, atomicTask);
                        resourceAgent.increaseTotalExecutionTime(taskExecutionTime);
                    }else{
                        TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
                        int TimeSlotPreviousExecutionTime = timeSlot.getExecutionTime();
                        timeSlot.addTask(atomicTask, taskExecutionTime);
                        if (taskExecutionTime > TimeSlotPreviousExecutionTime){
                            resourceAgent.increaseTotalExecutionTime(taskExecutionTime - TimeSlotPreviousExecutionTime);
                        }
                    }
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


}
