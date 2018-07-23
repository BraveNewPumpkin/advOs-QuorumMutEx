package Node;

public class FailedMessage extends SimpleTargetableMessage {
    public FailedMessage() {
    }

    public FailedMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "FailedMessage{} " + super.toString();
    }
}

