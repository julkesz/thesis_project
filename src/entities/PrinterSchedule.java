package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class PrinterSchedule implements Serializable {
    private ArrayList<TimeSlot> schedule;

    public PrinterSchedule() {
        this.schedule = new ArrayList<>();
    }
    public PrinterSchedule(ArrayList<TimeSlot> schedule) {
        this.schedule = schedule;
    }

    public ArrayList<TimeSlot> getSchedule() {
        return schedule;
    }

    public void addTimeSlot(int start){
        schedule.add(new TimeSlot(start, 0));
    }

    public boolean isEmpty(){
        return schedule.isEmpty();
    }

    @Override
    public String toString() {
        return "PrinterSchedule{" +
                "schedule=" + schedule +
                '}';
    }
}



