package Node;

public class ReceivedInquiry {
    private final int sourceUid;
    private final int sourceTimeStamp;

    public ReceivedInquiry(int sourceUid, int sourceTimeStamp) {
        this.sourceUid = sourceUid;
        this.sourceTimeStamp = sourceTimeStamp;
    }

    public int getSourceUid() {
        return sourceUid;
    }

    public int getSourceTimeStamp() {
        return sourceTimeStamp;
    }
}
