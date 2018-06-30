package Node;

public class FifoResponseMessage extends SimpleTargetableMessage {
    //stored as a plain string because mapping flipping jackson can't handle my abstraction
    private FifoRequestId fifoRequestId;

    public FifoResponseMessage() {
    }

    public FifoResponseMessage(int sourceUID, int target, FifoRequestId fifoRequestId) {
        super(sourceUID, target);
        this.fifoRequestId = fifoRequestId;
    }

    public String getFifoRequestIdAsString() {
        return fifoRequestId.getRequestId();
    }

    public void setFifoRequestIdAsString(String fifoRequestId) {
        this.fifoRequestId = new FifoRequestId();
        this.fifoRequestId.setRequestId(fifoRequestId);
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
