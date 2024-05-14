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

    @Override
    public String toString() {
        return "PrinterSchedule{" +
                "schedule=" + schedule +
                '}';
    }
}



