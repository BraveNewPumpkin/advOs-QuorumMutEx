package Node;

import java.util.UUID;

public class GrantMessage extends SimpleTargetableMessage implements RequestIdentifiable {
    private UUID requestId;

    public GrantMessage() {
    }

    public GrantMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId(){
        return requestId;
    }

    @Override
    public String toString() {
        return "GrantMessage{" +
                "requestId=" + requestId +
                "} " + super.toString();
    }

}

