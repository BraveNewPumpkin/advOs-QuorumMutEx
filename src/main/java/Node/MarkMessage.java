package Node;

public class MarkMessage extends RoundSynchronizableMessage<Integer> implements FifoRequest{
    private FifoRequestId fifoRequestId;

    public MarkMessage() {
    }

    public MarkMessage(int sourceUID, int snapshotNumber, FifoRequestId fifoRequestId) {
        super(sourceUID, snapshotNumber);

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
        return "MarkMessage{" +
                "} " + super.toString();
    }
}
