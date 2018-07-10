package Node;

import java.util.ArrayList;
import java.util.List;

public class SnapshotInfo {
    private int sentMessages;
    private int processedMessages;
    private final List<Integer> vectorClock;
    private boolean isActive;

    public SnapshotInfo() {
        this.sentMessages = 0;
        this.processedMessages = 0;
        vectorClock = new ArrayList<>();
    }

    public SnapshotInfo(SnapshotInfo other, List<Integer> currentVectorClock) {
        this.sentMessages = other.sentMessages;
        this.processedMessages = other.processedMessages;
        List<Integer> newVectorClock = new ArrayList<>(currentVectorClock);
        this.vectorClock = newVectorClock;
        this.isActive = other.isActive;
    }

    public int getSentMessages() {
        return sentMessages;
    }

    public void setSentMessages(int sentMessages) {
        this.sentMessages = sentMessages;
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

    public void setProcessedMessages(int processedMessages) {
        this.processedMessages = processedMessages;
    }

    public List<Integer> getVectorClock() {
        return vectorClock;
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
