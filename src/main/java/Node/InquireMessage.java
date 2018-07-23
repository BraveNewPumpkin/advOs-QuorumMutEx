package Node;

public class InquireMessage extends SimpleTargetableMessage {
    public InquireMessage() {
    }

    public InquireMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "InquireMessage{} " + super.toString();
    }
}

