package entities;

public class TimeSlotCalculation {
    int timeSlotNumber = -1;
    int executionTime = 0;

    public TimeSlotCalculation(int timeSlotNumber, int executionTime) {
        this.timeSlotNumber = timeSlotNumber;
        this.executionTime = executionTime;
    }

    public TimeSlotCalculation() {
    }

    public int getTimeSlotNumber() {
        return timeSlotNumber;
    }

    public void setTimeSlotNumber(int timeSlotNumber) {
        this.timeSlotNumber = timeSlotNumber;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }
}
