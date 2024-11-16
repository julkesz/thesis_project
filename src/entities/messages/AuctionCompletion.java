package entities.messages;

import java.io.Serializable;


public class AuctionCompletion implements Serializable {

    private long elapsedTime;


    public AuctionCompletion(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
