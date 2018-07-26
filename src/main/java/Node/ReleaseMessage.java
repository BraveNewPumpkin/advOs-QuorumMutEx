package Node;

import java.util.UUID;

public class ReleaseMessage extends NodeMessage implements RequestIdentifiable {
    private UUID requestId;

    public ReleaseMessage() {
    }

    public ReleaseMessage(int sourceUID, int scalarClock, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, scalarClock, criticalSectionNumber);
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId(){
        return requestId;
    }

    @Override
    public String toString() {
        return "ReleaseMessage{" +
                "requestId=" + requestId +
                "} " + super.toString();
    }
}

