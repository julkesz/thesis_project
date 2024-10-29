package agents;

import entities.PrinterSchedule;
import jade.core.Agent;


public class ResourceAgent extends Agent {
    protected int boardWidth;
    protected int boardLength;
    protected int maxHeight;
    protected int printingSpeed;

    protected int filament;
    protected PrinterSchedule printerSchedule;
    protected int totalExecutionTime;
    protected int totalSize;
    public static final int FILAMENT_REPLACEMENT_TIME = 20;
    public static final double BOARD_HEURISTICS = 0.8;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length == 5) {
            boardWidth = Integer.parseInt((String) args[0]);
            boardLength = Integer.parseInt((String) args[1]);
            maxHeight = Integer.parseInt((String) args[2]);
            printingSpeed = Integer.parseInt((String) args[3]);
            filament = Integer.parseInt((String) args[4]);
        } else {
            System.out.println("No arguments provided.");
        }

        printerSchedule = new PrinterSchedule(getLocalName(), boardWidth, boardLength, maxHeight, printingSpeed);
        totalExecutionTime = 0;
        totalSize = 0;
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

    public int getFilament() {
        return filament;
    }

    public PrinterSchedule getPrinterSchedule() {
        return printerSchedule;
    }

    public int getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setFilament(int filament) {
        this.filament = filament;
    }

    public void setTotalExecutionTime(int totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
}
