package Node;

public class YieldMessage extends SimpleTargetableMessage {
    public YieldMessage() {
    }

    public YieldMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "YieldMessage{} " + super.toString();
    }
}

