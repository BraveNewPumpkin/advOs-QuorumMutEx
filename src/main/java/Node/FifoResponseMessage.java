package Node;

public class FifoResponseMessage extends SimpleTargetableMessage implements FifoRequest{
    private FifoRequestId fifoRequestId;

    public FifoResponseMessage() {
    }

    public FifoResponseMessage(int sourceUID, int target, int sourceScalarClock, int sourceCriticalSectionNumber, FifoRequestId fifoRequestId) {
        super(sourceUID, target, sourceScalarClock, sourceCriticalSectionNumber);
        this.fifoRequestId = fifoRequestId;
    }

    public FifoRequestId getFifoRequestId() {
        return fifoRequestId;
    }

    public void setFifoRequestId(FifoRequestId fifoRequestId) {
        this.fifoRequestId = fifoRequestId;
    }

    @Override
    public String toString() {
        return "FifoResponseMessage{" +
                "fifoRequestId='" + fifoRequestId + '\'' +
                "} " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FifoResponseMessage that = (FifoResponseMessage) o;

        return fifoRequestId != null ? fifoRequestId.equals(that.fifoRequestId) : that.fifoRequestId == null;
    }
}
