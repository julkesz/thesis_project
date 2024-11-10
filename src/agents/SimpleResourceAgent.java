package agents;

import bahaviours.auctionresponder.AuctionAcceptanceBehaviour;
import bahaviours.auctionresponder.AuctionCompletionBehaviour;
import bahaviours.auctionresponder.AuctionProposalBehaviour;

public class SimpleResourceAgent extends ResourceAgent {

	protected void setup() {
		super.setup();

		System.out.println("Agent " + getLocalName() + " waiting for CFP...");

		addBehaviour(new AuctionProposalBehaviour());
		addBehaviour(new AuctionAcceptanceBehaviour());
		addBehaviour(new AuctionCompletionBehaviour(this));
	}
}

