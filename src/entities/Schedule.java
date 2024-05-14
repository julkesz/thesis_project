package entities;

import java.io.Serializable;
import java.util.TreeMap;

public class Schedule implements Serializable {
    private TreeMap<String,PrinterSchedule> printerSchedules;

    public Schedule() {
        this.printerSchedules = new TreeMap<>();
    }

    public Schedule(TreeMap<String,PrinterSchedule> printerSchedules) {
        this.printerSchedules = printerSchedules;
    }

    public TreeMap<String,PrinterSchedule> getPrinterSchedules() {
        return printerSchedules;
    }

    public void setPrinterSchedules(TreeMap<String,PrinterSchedule> printerSchedules) {
        this.printerSchedules = printerSchedules;
    }

    public void addTask(String printerName, AtomicTask task) {

        this.printerSchedules = printerSchedules;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "printerSchedules=" + printerSchedules +
                '}';
    }
}
