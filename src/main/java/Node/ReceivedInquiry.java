package Node;

public class ReceivedInquiry {
    private final int sourceUid;
    private final int sourceTimeStamp;
    private final int sourceCriticalSectionNumber;

    public ReceivedInquiry(int sourceUid, int sourceTimeStamp, int sourceCriticalSectionNumber) {
        this.sourceUid = sourceUid;
        this.sourceTimeStamp = sourceTimeStamp;
        this.sourceCriticalSectionNumber = sourceCriticalSectionNumber;
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

    @Override
    public String toString() {
        return "ReceivedInquiry{" +
                "sourceUid=" + sourceUid +
                ", sourceTimeStamp=" + sourceTimeStamp +
                ", sourceCriticalSectionNumber=" + sourceCriticalSectionNumber +
                '}';
    }
}
