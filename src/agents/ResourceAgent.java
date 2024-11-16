package agents;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.AtomicTask;
import entities.PrinterSchedule;
import entities.TimeSlot;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import utils.LocalDateTimeTypeAdapter;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


public class ResourceAgent extends Agent {
    private long startTime;
    private long elapsedTime;
    private int auctionInitiatorCount = 1;
    private int completionMessageCount = 0;

    protected int boardWidth;
    protected int boardLength;
    protected int maxHeight;
    protected int printingSpeed; //20-36 mm/h

    protected int originalFilament;
    protected int currentFilament;
    protected PrinterSchedule printerSchedule;
    protected int totalExecutionTime;
    protected boolean scanAllTimeSlots;
    public static final int FILAMENT_REPLACEMENT_TIME = 20;
    public static final double BOARD_HEURISTICS = 0.8;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length == 6) {
            boardWidth = Integer.parseInt((String) args[0]);
            boardLength = Integer.parseInt((String) args[1]);
            maxHeight = Integer.parseInt((String) args[2]);
            printingSpeed = Integer.parseInt((String) args[3]);
            originalFilament = Integer.parseInt((String) args[4]);
            currentFilament = Integer.parseInt((String) args[4]);
            if (Objects.equals(args[5].toString(), "scanall")){
                scanAllTimeSlots = true;
            } else if (Objects.equals(args[5].toString(), "scanlast")) {
                scanAllTimeSlots = false;
            } else{
                System.out.println("Wrong argument provided.");
            }
        } else {
            System.out.println("No arguments provided.");
        }

        printerSchedule = new PrinterSchedule(getLocalName(), boardWidth, boardLength, maxHeight, printingSpeed);
        totalExecutionTime = 0;

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("printer-service");
        sd.setName("PrinterAgentService");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getAuctionInitiatorCount() {
        return auctionInitiatorCount;
    }

    public void setAuctionInitiatorCount(int auctionInitiatorCount) {
        this.auctionInitiatorCount = auctionInitiatorCount;
    }

    public int getCompletionMessageCount() {
        return completionMessageCount;
    }

    public void increaseCompletionMessageCount() {
        this.completionMessageCount++;
    }
    public int getBoardWidth() {
        return boardWidth;
    }

    public int getBoardLength() {
        return boardLength;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getPrintingSpeed() {
        return printingSpeed;
    }

    public void setPrintingSpeed(int printingSpeed) {
        this.printingSpeed = printingSpeed;
    }

    public int getCurrentFilament() {
        return currentFilament;
    }

    public void setCurrentFilament(int filament) {
        this.currentFilament = filament;
    }

    public PrinterSchedule getPrinterSchedule() {
        return printerSchedule;
    }

    public int getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void increaseTotalExecutionTime(int time) {
        this.totalExecutionTime += time;
    }

    public int getTimeSlotOccupancy(int timeSlotNumber) {
        if (printerSchedule.getSchedule().isEmpty()){
            return 0;
        }
        TimeSlot timeSlot = printerSchedule.getSchedule().get(timeSlotNumber);
        return timeSlot.getOccupancy();
    }

    public int calculateTimeSlotNumber(AtomicTask atomicTask) {

        int taskSize = atomicTask.getLength() * atomicTask.getWidth();
        int boardSize = boardWidth * boardLength;
        if ((taskSize > boardSize) || (atomicTask.getHeight() > maxHeight)) {
            return -1;
        }
        if (printerSchedule.isEmpty()) {
            return 0;
        }

        ArrayList<TimeSlot> timeSlotList = printerSchedule.getSchedule();
        if (scanAllTimeSlots){
            for (int timeSlotNumber = 0; timeSlotNumber < timeSlotList.size(); timeSlotNumber++) {
                TimeSlot timeSlot = timeSlotList.get(timeSlotNumber);
                if ((timeSlot.getFilament() == atomicTask.getFilament())
                        && (timeSlot.getOccupancy() + taskSize <= ResourceAgent.BOARD_HEURISTICS * boardSize)) {
                    return timeSlotNumber;
                }
            }
        }
        int lastTimeSlotNumber = timeSlotList.size() - 1;
        int lastFilament = timeSlotList.get(lastTimeSlotNumber).getFilament();
        int timeSlotNumber = lastTimeSlotNumber;
        if ((lastFilament != atomicTask.getFilament()) ||
                (this.getTimeSlotOccupancy(lastTimeSlotNumber) + taskSize > ResourceAgent.BOARD_HEURISTICS * boardSize)) {
            timeSlotNumber++;
        }
        return timeSlotNumber;
    }

    public void generateJSONSchedule() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .setPrettyPrinting()
                .create();

        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String fileName = getLocalName() + "schedule_" + date + ".json";
        try (FileWriter fileWriter = new FileWriter("src/output/output_jsons/"+fileName)) {
            String jsonString = gson.toJson(printerSchedule);
            fileWriter.write(jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void completePrinterSchedule() {
        printerSchedule.setElapsedTime(elapsedTime);
        int startTime = 0;
        ArrayList<TimeSlot> timeSlotList = printerSchedule.getSchedule();
        for (TimeSlot timeSlot : timeSlotList){
            if (timeSlot.isFilamentChanged()){
                startTime += ResourceAgent.FILAMENT_REPLACEMENT_TIME;
            }
            timeSlot.setStart(startTime);
            timeSlot.setStop(startTime + timeSlot.getExecutionTime());
            startTime += timeSlot.getExecutionTime();
        }
    }

}
