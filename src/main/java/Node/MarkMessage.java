package Node;

public class MarkMessage extends RoundSynchronizableMessage {

    public MarkMessage() {
    }

    public MarkMessage(int sourceUID, int snapshotNumber) {
        super(sourceUID, snapshotNumber);
    }

    @Override
    public String toString() {
        return "MarkMessage{" +
                "} " + super.toString();
    }
}
