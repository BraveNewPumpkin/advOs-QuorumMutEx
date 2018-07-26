package Node;

import java.util.UUID;

public class RequestMessage extends NodeMessage  implements RequestIdentifiable{
    private UUID requestId;

    public RequestMessage() {
    }

    public RequestMessage(int sourceUID, int sourceTimestamp, int criticalSectionNumber, UUID requestId) {
        super(sourceUID, sourceTimestamp, criticalSectionNumber);
        this.requestId = requestId;
    }

    @Override
    public UUID getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "requestId=" + requestId +
                "} " + super.toString();
    }
}

