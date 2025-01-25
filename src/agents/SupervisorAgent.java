package agents;

import bahaviours.supervisor.TaskGroupingBehaviour;
import jade.core.Agent;


public class SupervisorAgent extends Agent {

    private long startTime;
    private String orderFileName;
    private int groupCount = 1;
    private String divisionMode = "random";


    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            try {
                // First argument: number of orders
                orderFileName = args[0].toString();
                // Second argument: number of groups
                groupCount = Integer.parseInt(args[1].toString());
                // Third argument: task division mode
                divisionMode = args[2].toString();
            } catch (Exception e) {
                System.err.println("Error parsing agent parameters: " + e.getMessage());
            }
        }

        // Add the behavior to divide tasks and send to printers
        addBehaviour(new TaskGroupingBehaviour());
    }

    public String getOrderFileName() {
        return orderFileName;
    }

    public int getGroupCount() {
        return groupCount;
    }

    public String getDivisionMode() {
        return divisionMode;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
