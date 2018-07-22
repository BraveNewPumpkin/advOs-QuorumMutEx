package Node;

public class FailedMessage extends SimpleTargetableMessage {
    public FailedMessage() {
    }

    public FailedMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }

    @Override
    public String toString() {
        return "FailedMessage{} " + super.toString();
    }
}

