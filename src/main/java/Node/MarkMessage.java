package Node;

public class MarkMessage extends NodeMessage {
    private int snapshotNumber;

    public MarkMessage() {
    }

    public MarkMessage(int sourceUID, int snapshotNumber) {
        super(sourceUID);
        this.snapshotNumber = snapshotNumber;
    }

    @Override
    public String toString() {
        return "MarkMessage{" +
                "snapshotNumber=" + snapshotNumber +
                "} " + super.toString();
    }
}
