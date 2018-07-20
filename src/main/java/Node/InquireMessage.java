package Node;

public class InquireMessage extends SimpleTargetableMessage {
    public InquireMessage() {
    }

    public InquireMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }
}

