package Node;

import java.util.UUID;

public class FailedMessage extends SimpleTargetableMessage implements RequestIdentifiable {
    private UUID requestId;

    public FailedMessage() {
    }

    public FailedMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId(){
        return requestId;
    }

    @Override
    public String toString() {
        return "FailedMessage{" +
                "requestId=" + requestId +
                "} " + super.toString();
    }
}

