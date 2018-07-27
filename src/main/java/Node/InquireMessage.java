package Node;

import java.util.UUID;

public class InquireMessage extends SimpleTargetableMessage implements RequestIdentifiable {
    private UUID requestId;

    public InquireMessage() {
    }

    public InquireMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId(){
        return requestId;
    }

    @Override
    public String toString() {
        return "InquireMessage{} " + super.toString();
    }
}

