package Node;

public class FifoResponseMessage extends SimpleTargetableMessage {
    //stored as a plain string because mapping flipping jackson can't handle my abstraction
    private String fifoRequestId;

    public FifoResponseMessage() {
    }

    public FifoResponseMessage(int sourceUID, int target, FifoRequestId fifoRequestId) {
        super(sourceUID, target);
        this.fifoRequestId = fifoRequestId.getRequestId();
    }

    public FifoRequestId getFifoRequestId() {
        FifoRequestId fifoRequestId = new FifoRequestId();
        fifoRequestId.setRequestId(this.fifoRequestId);
        return fifoRequestId;
    }

    public String getFifoRequestIdAsString() {
        return fifoRequestId;
    }

    public void setFifoRequestId(String fifoRequestId) {
        this.fifoRequestId = fifoRequestId;
    }

    public void setFifoRequestId(FifoRequestId fifoRequestId) {
        this.fifoRequestId = fifoRequestId.getRequestId();
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
