package Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnapshotInfo {
    private int sentMessages;
    private int processedMessages;
    private List<Integer> vectorClock;
    private boolean isActive;
    public int[] sentMessagesToNodes;
    public int[] receivedMessagesFromNodes;

    public SnapshotInfo() {
        this.sentMessages = 0;
        this.processedMessages = 0;
        vectorClock = new ArrayList<>();
        sentMessagesToNodes=new int[5];
        Arrays.fill(sentMessagesToNodes,0);
        receivedMessagesFromNodes=new int[5];
        Arrays.fill(receivedMessagesFromNodes,0);
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

    public void setSentMessages(int sentMessages) {
        this.sentMessages = sentMessages;
    }

    public void setProcessedMessages(int processedMessages) {
        this.processedMessages = processedMessages;
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
