package agents;

import bahaviours.auctionresponder.AuctionCompletionBehaviour;
import bahaviours.auctionresponder.RingBehaviour;

public class RingResourceAgent extends ResourceAgent {
    private String nextAgent;
    private String supervisorAgent;

    @Override
    protected void setup() {

        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length == 7) {
            try {
                nextAgent = args[6].toString();
            } catch (Exception e) {
                System.err.println("Error parsing agent parameters: " + e.getMessage());
            }
        }


        System.out.println(getLocalName() + " initialized. Next agent: " + nextAgent);

        // Add the cyclic behaviour to handle messages
        addBehaviour(new RingBehaviour());
        addBehaviour(new AuctionCompletionBehaviour());
    }

    public String getNextAgent() {
        return nextAgent;
    }

    public void setNextAgent(String nextAgent) {
        this.nextAgent = nextAgent;
    }

    public String getSupervisorAgent() {
        return supervisorAgent;
    }

    public void setSupervisorAgent(String supervisorAgent) {
        this.supervisorAgent = supervisorAgent;
    }
}

