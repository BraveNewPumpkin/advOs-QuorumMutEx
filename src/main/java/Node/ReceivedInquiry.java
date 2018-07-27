package Node;

import java.util.UUID;

public class ReceivedInquiry {
    private final int sourceUid;
    private final int sourceTimeStamp;
    private final int sourceCriticalSectionNumber;
    private final UUID requestId;

    public ReceivedInquiry(int sourceUid, int sourceTimeStamp, int sourceCriticalSectionNumber, UUID requestId) {
        this.sourceUid = sourceUid;
        this.sourceTimeStamp = sourceTimeStamp;
        this.sourceCriticalSectionNumber = sourceCriticalSectionNumber;
        this.requestId = requestId;
    }

    public int getSourceUid() {
        return sourceUid;
    }

    public int getSourceTimeStamp() {
        return sourceTimeStamp;
    }

    public int getSourceCriticalSectionNumber() {
        return sourceCriticalSectionNumber;
    }

    public UUID getRequestId() {
        return requestId;
    }

    @Override
    public String toString() {
        return "ReceivedInquiry{" +
                "sourceUid=" + sourceUid +
                ", sourceTimeStamp=" + sourceTimeStamp +
                ", sourceCriticalSectionNumber=" + sourceCriticalSectionNumber +
                ", requestId=" + requestId +
                '}';
    }
}
