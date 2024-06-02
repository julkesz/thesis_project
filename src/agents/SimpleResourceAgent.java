package agents; /**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

import entities.AtomicTask;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.FailureException;
import model.InformMessage;

import java.io.IOException;
import java.util.ArrayList;

public class SimpleResourceAgent extends ResourceAgent {

	protected void setup() {

		super.setup();

		System.out.println("Agent "+getLocalName()+" waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );

		addBehaviour(new ContractNetResponder(this, template) {
			@Override
			protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				AtomicTask task = new AtomicTask();
				try {
					task = (AtomicTask) cfp.getContentObject();
					System.out.println("Agent "+getLocalName()+": CFP received from "+cfp.getSender().getLocalName()+". Action is "+cfp.getContentObject());
				} catch (UnreadableException e) {
					throw new RuntimeException(e);
				}
				int proposal = evaluateTask(task);
				if (proposal > 0) {
					// We provide a proposal
					System.out.println("Agent "+getLocalName()+": Proposing "+proposal);
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(proposal));
					return propose;
				}
				else {
					// We refuse to provide a proposal
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("evaluation-failed");
				}
			}

			@Override
			protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
				AtomicTask task = new AtomicTask();
				int timeSlot = 0;

				try {
					task = (AtomicTask) cfp.getContentObject();
					timeSlot = calculateTimeSlot(task);
					/*
					if(atomicTaskList.size() == timeSlot){
						atomicTaskList.add(new ArrayList<>());
					}

					atomicTaskList.get(timeSlot).add(task);
					*/
					int taskSize = task.getLength()* task.getWidth();
					if(totalSize!=0 && totalSize + taskSize > BOARD_HEURISTICS * boardSize) {
						totalSize = taskSize;
					}else{
						totalSize = totalSize + taskSize;
					}
					totalExecutionTime = Integer.parseInt(propose.getContent());
				} catch (UnreadableException e) {
					throw new RuntimeException(e);
				}

				InformMessage messageContent = new InformMessage(timeSlot, task);

				ACLMessage inform = accept.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				try {
					inform.setContentObject(messageContent);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return inform;
			}

			protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			}
		} );
	}

	private int evaluateTask(AtomicTask task) {
		int taskSize =  task.getLength() * task.getWidth();
		if (taskSize > boardSize) {
			return 0;
		}

		int executionTime = task.getExecutionTime();
		/*
		if (atomicTaskList.isEmpty()){
			if (filament != task.getFilament()){
				executionTime = executionTime + FILAMENT_REPLACEMENT_TIME;
			}
		} else{
			int lastTimeSlot = atomicTaskList.size() - 1;
			int lastFilament = atomicTaskList.get(lastTimeSlot).get(0).getFilament();
			int lastTimeSlotSize = atomicTaskList.get(lastTimeSlot).size();
			AtomicTask lastTask = atomicTaskList.get(lastTimeSlot).get(lastTimeSlotSize-1);
			if (lastFilament != task.getFilament()){
				executionTime = totalExecutionTime + FILAMENT_REPLACEMENT_TIME + executionTime;
			} else if(totalSize + taskSize > BOARD_HEURISTICS * boardSize) {
				executionTime = totalExecutionTime + executionTime;
			} else{
				executionTime = Math.max(executionTime, totalExecutionTime);
			}
			if (lastTask.equals(task)){
				executionTime--;
			}

		}

		*/

		return executionTime;
	}


	private int calculateTimeSlot(AtomicTask task) {
		int taskSize =  task.getLength() * task.getWidth();
		int timeSlot = 0;
		/*
		if (!atomicTaskList.isEmpty()){
			int lastTimeSlot = atomicTaskList.size() - 1;
			if (atomicTaskList.get(lastTimeSlot).get(0).getFilament() != task.getFilament()
			|| totalSize + taskSize > BOARD_HEURISTICS * boardSize){
				timeSlot = lastTimeSlot + 1;
			} else{
				timeSlot =  lastTimeSlot;
			}
		}

		*/
		return timeSlot;
	}

	private boolean performTask() {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
	}

	public int getFilament() {
		return filament;
	}
}

