package Node;

public class CsRequest implements Comparable<CsRequest> {
    private final int sourceUid;
    private final int sourceTimestamp;

    public CsRequest(int sourceUid, int sourceTimestamp) {
        this.sourceUid = sourceUid;
        this.sourceTimestamp = sourceTimestamp;
    }

    public int getSourceUid() {
        return sourceUid;
    }

    public int getSourceTimestamp() {
        return sourceTimestamp;
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
}
