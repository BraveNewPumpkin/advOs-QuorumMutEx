package Node;

public class SnapshotInfo {
    private int sentMessages;
    private int processedMessages;

    public SnapshotInfo() {
        this.sentMessages = 0;
        this.processedMessages = 0;
    }

    public int getSentMessages() {
        return sentMessages;
    }

    public void incrementSentMessages() {
        this.sentMessages++;
    }

    public int getProcessedMessages() {
        return processedMessages;
    }

    public void incrementProcessedMessages() {
        this.processedMessages++;
    }

}
