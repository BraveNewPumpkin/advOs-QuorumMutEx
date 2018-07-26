package Node;

import java.util.UUID;

public class CsRequest implements Comparable<CsRequest> {
    private final int sourceUid;
    private final int sourceTimestamp;
    private final UUID requestId;

    public CsRequest(int sourceUid, int sourceTimestamp, UUID requestId) {
        this.sourceUid = sourceUid;
        this.sourceTimestamp = sourceTimestamp;
        this.requestId = requestId;
    }

    public int getSourceUid() {
        return sourceUid;
    }

    public int getSourceTimestamp() {
        return sourceTimestamp;
    }

    public UUID getRequestId() {
        return requestId;
    }

    @Override
    public int compareTo(CsRequest o) {
        if(sourceTimestamp < o.sourceTimestamp) {
            return -1;
        } else if(sourceTimestamp == o.sourceTimestamp) {
            if(sourceUid < o.sourceUid) {
                return -1;
            } else if(sourceUid == o.sourceUid) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "CsRequest{" +
                "sourceUid=" + sourceUid +
                ", sourceTimestamp=" + sourceTimestamp +
                ", requestId=" + requestId +
                '}';
    }
}
