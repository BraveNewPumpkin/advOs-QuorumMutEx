package Node;

public class InquireMessage extends SimpleTargetableMessage {
    private int sourceTimeStamp;

    public InquireMessage() {
    }

    public InquireMessage(int sourceUID, int target, int timeStamp) {
        super(sourceUID, target);
        sourceTimeStamp = timeStamp;
    }

    public int getSourceTimeStamp() {
        return sourceTimeStamp;
    }

    @Override
    public String toString() {
        return "InquireMessage{" +
                "sourceTimeStamp=" + sourceTimeStamp +
                "} " + super.toString();
    }
}

