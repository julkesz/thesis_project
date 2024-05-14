package Agents; /**
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
import jade.core.Agent;
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
import java.util.Arrays;
import java.util.List;

public class ResourceAgent extends Agent {

	private int filament;
	private int boardSize;
	private ArrayList<ArrayList<AtomicTask>> atomicTasksList;
	private int totalExecutionTime;
	private int totalSize;
	static final int filamentReplacementTime = 20;

	static final double boardHeuristics = 0.8;

	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length == 2) {
			boardSize = Integer.parseInt((String) args[0]);;
			filament = Integer.parseInt((String) args[1]);;
			System.out.println("Agent "+getLocalName()+" has board of size " + boardSize + " and filament number " + filament +".");
		}
		else {
			System.out.println("No arguments provided.");
		}

		atomicTasksList = new ArrayList();
		totalExecutionTime = 0;
		totalSize = 0;

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

					if(atomicTasksList.size() == timeSlot){
						atomicTasksList.add(new ArrayList<>());
					}

					atomicTasksList.get(timeSlot).add(task);

					int taskSize = task.getLength()* task.getWidth();
					if(totalSize!=0 && totalSize + taskSize > boardHeuristics * boardSize) {
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

		long executionTime = task.getExecutionTime();

		if (atomicTasksList.isEmpty()){
			if (filament != task.getFilament()){
				executionTime = executionTime + filamentReplacementTime;
			}
		} else{
			int lastTimeSlot = atomicTasksList.size() - 1;
			int lastFilament = atomicTasksList.get(lastTimeSlot).get(0).getFilament();
			int lastTimeSlotSize = atomicTasksList.get(lastTimeSlot).size();
			AtomicTask lastTask = atomicTasksList.get(lastTimeSlot).get(lastTimeSlotSize-1);
			if (lastFilament != task.getFilament()){
				executionTime = totalExecutionTime + filamentReplacementTime + executionTime;
			} else if(totalSize + taskSize > boardHeuristics * boardSize) {
				executionTime = totalExecutionTime + executionTime;
			} else{
				executionTime = Math.max(executionTime, totalExecutionTime);
			}
			if (lastTask.equals(task)){
				executionTime--;
			}

		}

		return (int) executionTime;
	}


	private int calculateTimeSlot(AtomicTask task) {
		int taskSize =  task.getLength() * task.getWidth();
		int timeSlot = 0;

		if (!atomicTasksList.isEmpty()){
			int lastTimeSlot = atomicTasksList.size() - 1;
			if (atomicTasksList.get(lastTimeSlot).get(0).getFilament() != task.getFilament()
			|| totalSize + taskSize > boardHeuristics * boardSize){
				timeSlot = lastTimeSlot + 1;
			} else{
				timeSlot =  lastTimeSlot;
			}
		}
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

