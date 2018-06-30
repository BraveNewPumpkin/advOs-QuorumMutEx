package Node;

import java.util.ArrayList;
import java.util.List;

public class SnapshotInfo {
    private int sentMessages;
    private int processedMessages;
    private List<Integer> vectorClock;
    private boolean isActive;

    public SnapshotInfo() {
        this.sentMessages = 0;
        this.processedMessages = 0;
        vectorClock = new ArrayList<>();
    }

    public int getSentMessages() {
        return sentMessages;
    }

    public void incrementSentMessages() {
        this.sentMessages++;
    }

    public void incrementProcessedMessages() {
        this.processedMessages++;
    }

    public int getProcessedMessages() {
        return processedMessages;
    }

    public List<Integer> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(List<Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "SnapshotInfo{" +
                "sentMessages=" + sentMessages +
                ", processedMessages=" + processedMessages +
                ", vectorClock=" + vectorClock +
                ", isActive=" + isActive +
                '}';
    }
}
