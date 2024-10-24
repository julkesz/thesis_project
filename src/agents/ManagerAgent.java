package agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.PrinterSchedule;
import entities.Schedule;
import entities.TimeSlot;
import jade.lang.acl.UnreadableException;
import model.InformMessage;
import utils.LocalDateTimeTypeAdapter;
import utils.OrderReader;
import entities.AtomicTask;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class ManagerAgent extends Agent {
	private int orderCount;
	static final int filamentReplacementTime = 20;
	private int nResponders;

	private TreeMap<String,ArrayList<ArrayList<AtomicTask>>> temporarySchedule = new TreeMap<>();

	private Schedule finalSchedule = new Schedule();

	private int nTasks;
	private int nTasksAllocated;

	private TreeMap<String,Integer> printerFilaments = new TreeMap<>();

	protected void setup() {

		// Read names of printers as arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			nResponders = args.length;

			for (int i = 0; i < args.length; ++i) {
				printerFilaments.put((String) args[i], i+1);
				temporarySchedule.put((String) args[i],new ArrayList<>());
			}
		}else {
			System.out.println("No responder specified.");
		}

		System.out.println("FILAMENTS: " + printerFilaments);

		OrderReader orderReader = new OrderReader(orderCount);
		orderReader.retrieveOrders();
		nTasks = orderReader.getAtomicTasksList().size();

		for (int i = 0; i< nTasks; i++) {

			AtomicTask task = (AtomicTask) orderReader.getAtomicTasksList().get(i);

			// Fill the CFP message
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			for (int j = 0; j < args.length; ++j) {
				msg.addReceiver(new AID((String) args[j], AID.ISLOCALNAME));
			}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

			try {
				msg.setContentObject(task);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			addBehaviour(new ContractNetInitiator(this, msg) {

				protected void handlePropose(ACLMessage propose, Vector v) {
				}

				protected void handleRefuse(ACLMessage refuse) {
					System.out.println("Agent "+refuse.getSender().getName()+" refused");
				}

				protected void handleFailure(ACLMessage failure) {
					if (failure.getSender().equals(myAgent.getAMS())) {
						// FAILURE notification from the JADE runtime: the receiver
						// does not exist
						System.out.println("Responder does not exist");
					}
					else {
						System.out.println("Agent "+failure.getSender().getName()+" failed");
					}
					// Immediate failure --> we will not receive a response from this agent
					nResponders--;
				}

				protected void handleAllResponses(Vector responses, Vector acceptances) {
					if (responses.size() < nResponders) {
						// Some responder didn't reply within the specified timeout
						System.out.println("Timeout expired: missing "+(nResponders - responses.size())+" responses");
					}
					// Evaluate proposals.
					int bestProposal = Integer.MAX_VALUE;
					AID bestProposer = null;
					ACLMessage accept = null;

					Enumeration e = responses.elements();
					while (e.hasMoreElements()) {
						ACLMessage msg = (ACLMessage) e.nextElement();
						if (msg.getPerformative() == ACLMessage.PROPOSE) {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							acceptances.addElement(reply);
							int proposal = Integer.parseInt(msg.getContent());
							if (proposal < bestProposal) {
								bestProposal = proposal;
								bestProposer = msg.getSender();
								accept = reply;
							}
						}
					}
					// Accept the proposal of the best proposer
					if (accept != null) {
						System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getLocalName());
						accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					}
				}

				protected void handleInform(ACLMessage inform) {
					InformMessage messageContent;
					try {
						messageContent = (InformMessage) inform.getContentObject();
					} catch (UnreadableException e) {
						throw new RuntimeException(e);
					}

					ArrayList<ArrayList<AtomicTask>> printerTemporarySchedule = temporarySchedule.get(inform.getSender().getLocalName());

					int timeSlot = messageContent.getTimeSlot();
					if (printerTemporarySchedule.size() == timeSlot){
						printerTemporarySchedule.add(new ArrayList<>());
					}
					AtomicTask task = messageContent.getTask();
					printerTemporarySchedule.get(timeSlot).add(task);

					nTasksAllocated++;

					if (nTasksAllocated == nTasks){
						generateFinalSchedule();
						generateJSONSchedule();
					}

				}
			} );
		}
	}

	private void generateFinalSchedule() {
		temporarySchedule.forEach(
				(printer, printerTemporarySchedule) -> {

					finalSchedule.getPrinterSchedules().put(printer,new PrinterSchedule("",1,1,1,1));
					int start = 0;
					Integer printerFilament = printerFilaments.get(printer);


					for (ArrayList<AtomicTask> taskArray : printerTemporarySchedule) {
						if (taskArray.get(0).getFilament() != printerFilament){
							start = start + filamentReplacementTime;
						}
						int maxExecutionTime = taskArray.stream()
								.mapToInt(AtomicTask::getHeight).max().orElseThrow(NoSuchElementException::new);
						int stop = start + maxExecutionTime;

						TimeSlot timeSlot = new TimeSlot(start,stop);
						timeSlot.setTasks(taskArray);
						finalSchedule.getPrinterSchedules().get(printer).getSchedule().add(timeSlot);
						start = start + maxExecutionTime;
						printerFilament = taskArray.get(0).getFilament();
					}
				});
	}

	private void generateJSONSchedule() {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
				.setPrettyPrinting()
				.create();

		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		String fileName = "output" + date + ".json";
		try (FileWriter fileWriter = new FileWriter("src/output/output_jsons/"+fileName)) {
			String jsonString = gson.toJson(finalSchedule);
			fileWriter.write(jsonString);
			System.out.println("JSON Schedule generated");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}


