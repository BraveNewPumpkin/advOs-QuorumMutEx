package Node;

public class GrantMessage extends SimpleTargetableMessage {
    public GrantMessage() {
    }

    public GrantMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "GrantMessage{} " + super.toString();
    }
}

