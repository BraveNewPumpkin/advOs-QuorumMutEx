package Node;

import java.util.Arrays;
import java.util.List;

public class MapMessage extends SimpleTargetableMessage implements FifoRequest{
    private List<Integer> VectorClock;
    private FifoRequestId fifoRequestId;

    public MapMessage() {
    }

    public MapMessage(int sourceUID, int target, List<Integer> VectorClock, FifoRequestId fifoRequestId) {
        super(sourceUID, target);
        this.VectorClock = VectorClock;
        this.fifoRequestId = fifoRequestId;
    }

    public List<Integer> getVectorClock() {
        return VectorClock;
    }

    public void setVectorClock(List<Integer> vectorClock) {
        VectorClock = vectorClock;
    }

    public FifoRequestId getFifoRequestId() {
        return fifoRequestId;
    }

    public void setFifoRequestId(FifoRequestId fifoRequestId) {
        this.fifoRequestId = fifoRequestId;
    }

    @Override
    public String toString() {
        return "MapMessage{" + "VectorCLock" + VectorClock +
        "} " + super.toString();
    }
}

