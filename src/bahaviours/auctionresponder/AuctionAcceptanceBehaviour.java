package bahaviours.auctionresponder;

import agents.AdvancedResourceAgent;
import agents.ResourceAgent;
import bahaviours.auctioninitiator.AuctionInitiationBehaviour;
import entities.AtomicTask;
import entities.messages.AuctionProposal;
import entities.PrinterSchedule;
import entities.TimeSlot;
import entities.messages.AuctionRequest;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.ArrayList;
import java.util.List;
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
                            Objects.equals(((AdvancedResourceAgent) resourceAgent).getParallelAuctionMode(), "newauction") &&
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
                                /*
                                AID parentInitiator = proposalAcceptanceMessage.getSender();
                                String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());
                                String originalConversationId = proposalAcceptanceMessage.getConversationId();


                                ACLMessage parentInform = new ACLMessage(ACLMessage.INFORM);
                                parentInform.addReceiver(parentInitiator); // Parent who delegated the task
                                parentInform.setConversationId(originalConversationId); // Pass the original conversation ID
                                parentInform.addReplyTo(parentInitiator); // Include the original auction initiator's AID
                                parentInform.setContent(atomicTaskId); // Example status
                                parentInform.setInReplyTo(proposalAcceptanceMessage.getReplyWith());
                                System.out.println(resourceAgent.getLocalName() + " sending INFORM message to " + parentInitiator.getLocalName() +  " with IN REPLY TO: " + proposalAcceptanceMessage.getReplyWith());
                                myAgent.send(parentInform);*/
                            }
                        });

                        resourceAgent.addBehaviour(subAuctionSequence);

                        return;
                    }

                    PrinterSchedule printerSchedule = resourceAgent.getPrinterSchedule();
                    int taskExecutionTime = (int) Math.ceil((float) atomicTask.getHeight() / resourceAgent.getPrintingSpeed() * 60);

                    if (timeSlotNumber == printerSchedule.getSchedule().size()) {
                        boolean filamentReplacementFlag = false;
                        if (atomicTask.getFilament() != resourceAgent.getCurrentFilament()) {
                            filamentReplacementFlag = true;
                            resourceAgent.setCurrentFilament(atomicTask.getFilament());
                            resourceAgent.increaseTotalExecutionTime(ResourceAgent.FILAMENT_REPLACEMENT_TIME);
                        }
                        printerSchedule.addTimeSlot(filamentReplacementFlag, atomicTask);
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
                    /*
                    AID parentInitiator = proposalAcceptanceMessage.getSender();
                    String atomicTaskId = String.valueOf(atomicTask.getAtomicTaskId());
                    String originalConversationId = proposalAcceptanceMessage.getConversationId();


                    ACLMessage parentInform = new ACLMessage(ACLMessage.INFORM);
                    parentInform.addReceiver(parentInitiator); // Parent who delegated the task
                    parentInform.setConversationId(originalConversationId); // Pass the original conversation ID
                    parentInform.addReplyTo(parentInitiator); // Include the original auction initiator's AID
                    parentInform.setContent(atomicTaskId); // Example status
                    parentInform.setInReplyTo(proposalAcceptanceMessage.getReplyWith());
                    System.out.println(resourceAgent.getLocalName() + " sending INFORM message to " + parentInitiator.getLocalName() +  " with IN REPLY TO: " + proposalAcceptanceMessage.getReplyWith());
                    myAgent.send(parentInform);
*/
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
