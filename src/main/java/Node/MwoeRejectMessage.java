package Node;

public class MwoeRejectMessage extends SimpleTargetableMessage {
    public MwoeRejectMessage() {
    }

    public MwoeRejectMessage(int sourceUID, int phaseNumber, int target) {
        super(sourceUID, phaseNumber, target);
    }

    @Override
    public String toString() {
        return "MwoeRejectMessage{} " + super.toString();
    }
}
