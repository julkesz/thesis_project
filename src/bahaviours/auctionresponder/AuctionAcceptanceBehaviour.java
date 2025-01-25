package bahaviours.auctionresponder;

import agents.AdvancedResourceAgent;
import agents.ResourceAgent;
import bahaviours.auctioninitiator.AuctionInitiationBehaviour;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import entities.PrinterSchedule;
import entities.TimeSlot;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.Objects;

public class AuctionAcceptanceBehaviour extends CyclicBehaviour {

    public void action() {
        ACLMessage proposalAcceptanceMessage = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));

        if (proposalAcceptanceMessage != null) {
            try {
                AuctionProposal auctionProposal;
                int timeSlotNumber = 0;

                try {
                    auctionProposal = (AuctionProposal) proposalAcceptanceMessage.getContentObject();
                    AtomicTask atomicTask = auctionProposal.getAtomicTask();
                    ResourceAgent resourceAgent = (ResourceAgent) myAgent;
                    timeSlotNumber = resourceAgent.calculateTimeSlotNumber(atomicTask);

                    if (resourceAgent instanceof AdvancedResourceAgent &&
                            Objects.equals(((AdvancedResourceAgent) resourceAgent).getParallelAcceptanceMode(), "startauction") &&
                            timeSlotNumber != auctionProposal.getTimeSlotNumber()) {

                        System.out.println(resourceAgent.getLocalName() +
                                " calculated different time slot for atomic task " + atomicTask.getAtomicTaskId() +
                                ". Initiating a new auction.");

                        SequentialBehaviour subAuctionSequence = new SequentialBehaviour(myAgent);

                        // Step 1: Add the sub-auction behavior
                        subAuctionSequence.addSubBehaviour(new AuctionInitiationBehaviour(
                                resourceAgent,
                                atomicTask,
                                ((AdvancedResourceAgent) resourceAgent).getReceivers()
                        ));

                        // Step 2: Add a behavior to send INFORM after the sub-auction completes
                        subAuctionSequence.addSubBehaviour(new OneShotBehaviour(myAgent) {
                            @Override
                            public void action() {

                                String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());

                                ACLMessage reply = proposalAcceptanceMessage.createReply();
                                reply.setPerformative(ACLMessage.INFORM);
                                reply.setContent(atomicTaskId);
                                myAgent.send(reply);
                            }
                        });

                        resourceAgent.addBehaviour(subAuctionSequence);

                        return;
                    }

                    PrinterSchedule printerSchedule = resourceAgent.getPrinterSchedule();
                    int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / resourceAgent.getPrintingSpeed() * 60);

                    if (timeSlotNumber == printerSchedule.getSchedule().size()) {
                        boolean materialReplacementFlag = false;
                        if (atomicTask.getMaterial() != resourceAgent.getCurrentMaterial()) {
                            materialReplacementFlag = true;
                            resourceAgent.setCurrentMaterial(atomicTask.getMaterial());
                            resourceAgent.increaseTotalExecutionTime(ResourceAgent.MATERIAL_REPLACEMENT_TIME);
                        }
                        printerSchedule.addTimeSlot(materialReplacementFlag, atomicTask);
                        resourceAgent.increaseTotalExecutionTime(taskExecutionTime);
                    } else {
                        TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
                        int timeSlotPreviousExecutionTime = timeSlot.getExecutionTime();
                        timeSlot.addTask(atomicTask, taskExecutionTime);
                        if (taskExecutionTime > timeSlotPreviousExecutionTime) {
                            resourceAgent.increaseTotalExecutionTime(taskExecutionTime - timeSlotPreviousExecutionTime);
                        }
                    }
                    System.out.println(resourceAgent.getLocalName() + " added atomic task " + atomicTask.getAtomicTaskId() + " to timeslot " + timeSlotNumber);

                    String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());

                    ACLMessage reply = proposalAcceptanceMessage.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(atomicTaskId);
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
