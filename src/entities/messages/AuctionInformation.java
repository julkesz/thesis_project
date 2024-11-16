package entities.messages;


import java.io.Serializable;

public class AuctionInformation implements Serializable {

    private long startTime;
    private int auctionInitiatorCount;

    public AuctionInformation(long startTime, int auctionInitiatorNumber) {
        this.startTime = startTime;
        this.auctionInitiatorCount = auctionInitiatorNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getAuctionInitiatorCount() {
        return auctionInitiatorCount;
    }

    public void setAuctionInitiatorCount(int auctionInitiatorCount) {
        this.auctionInitiatorCount = auctionInitiatorCount;
    }
}
