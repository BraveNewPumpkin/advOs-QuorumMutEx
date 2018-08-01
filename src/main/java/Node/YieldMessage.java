package Node;

import java.util.UUID;

public class YieldMessage extends SimpleTargetableMessage implements RequestIdentifiable {
    private UUID requestId;

    public YieldMessage() {
    }

    public YieldMessage(int sourceUID, int target, int scalarClock, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, target, scalarClock, criticalSectionNumber);
        this.requestId = requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId(){
        return requestId;
    }

    @Override
    public String toString () {
        return "YieldMessage{" +
                "requestId=" + requestId +
                "} " + super.toString();
    }
}
