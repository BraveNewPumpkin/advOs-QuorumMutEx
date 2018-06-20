package Node;

public class SnapshotInfo {
    private int sentMessages;
    private int processedMessages;
    private int snapshotNumber;

    public SnapshotInfo() {
        this.sentMessages = 0;
        this.processedMessages = 0;
        this.snapshotNumber = 0;
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

    public void incrementSnapshotNumber() {
        this.snapshotNumber++;
    }

    public int getProcessedMessages() {
        return processedMessages;
    }

    public int getSnapshotNumber() {
        return snapshotNumber;
    }
}
