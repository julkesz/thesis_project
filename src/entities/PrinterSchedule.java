package entities;

import java.io.Serializable;
import java.util.ArrayList;

public class PrinterSchedule implements Serializable {
    private String printerName;
    private int boardWidth;
    private int boardLength;
    private int maxHeight;
    private int printingSpeed;
    private ArrayList<TimeSlot> schedule;

    public PrinterSchedule(String printerName, int boardWidth, int boardLength, int maxHeight, int printingSpeed) {
        this.printerName = printerName;
        this.boardWidth = boardWidth;
        this.boardLength = boardLength;
        this.maxHeight = maxHeight;
        this.printingSpeed = printingSpeed;
        this.schedule = new ArrayList<>();
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

    public void setBoardWidth(int boardWidth) {
        this.boardWidth = boardWidth;
    }

    public int getBoardLength() {
        return boardLength;
    }

    public void setBoardLength(int boardLength) {
        this.boardLength = boardLength;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getPrintingSpeed() {
        return printingSpeed;
    }

    public void setPrintingSpeed(int printingSpeed) {
        this.printingSpeed = printingSpeed;
    }

    public void setSchedule(ArrayList<TimeSlot> schedule) {
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
                "printerName='" + printerName + '\'' +
                ", boardWidth=" + boardWidth +
                ", boardLength=" + boardLength +
                ", maxHeight=" + maxHeight +
                ", printingSpeed=" + printingSpeed +
                ", schedule=" + schedule +
                '}';
    }
}



