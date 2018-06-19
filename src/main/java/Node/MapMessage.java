package Node;

import java.util.Arrays;

public class MapMessage extends SimpleTargetableMessage {
    int[] VectorClock;
    public MapMessage() {
    }

    public MapMessage(int sourceUID, int target, int[] VectorClock) {

        super(sourceUID, target);
        this.VectorClock =VectorClock;
    }

    public int[] getVectorClock() {
        return VectorClock;
    }

    public void setVectorClock(int[] vectorClock) {
        VectorClock = vectorClock;
    }

    @Override
    public String toString() {
        return "MapMessage{" + "VectorCLock" + Arrays.toString(VectorClock) +
        "} " + super.toString();
    }
}

