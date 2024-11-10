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
import java.util.Calendar;


public class ResourceAgent extends Agent {
    private int auctionInitiatorCount = 1;
    private int completionMessageCount = 0;

    protected int boardWidth;
    protected int boardLength;
    protected int maxHeight;
    protected int printingSpeed; //20-36 mm/h

    protected int filament;
    protected PrinterSchedule printerSchedule;
    protected int totalExecutionTime;
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

    public int getFilament() {
        return filament;
    }

    public PrinterSchedule getPrinterSchedule() {
        return printerSchedule;
    }

    public int getTotalExecutionTime() {
        return totalExecutionTime;
    }


    public void setFilament(int filament) {
        this.filament = filament;
    }

    public void setTotalExecutionTime(int totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public int getLastTimeSlotOccupancy() {
        if (printerSchedule.getSchedule().isEmpty()){
            return 0;
        }
        int lastTimeSlotNumber = printerSchedule.getSchedule().size() - 1;
        TimeSlot lastTimeSlot = printerSchedule.getSchedule().get(lastTimeSlotNumber);
        int lastTimeSlotOccupancy = 0;
        for (AtomicTask task : lastTimeSlot.getTasks()){
            lastTimeSlotOccupancy += task.getWidth() * task.getLength();
        }
        return lastTimeSlotOccupancy;
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

}
