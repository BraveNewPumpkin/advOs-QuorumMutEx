package Node;

import java.util.Arrays;
import java.util.List;

public class MapMessage extends SimpleTargetableMessage {
    List<Integer> VectorClock;
    public MapMessage() {
    }

    public MapMessage(int sourceUID, int target, List<Integer> VectorClock) {

        super(sourceUID, target);
        this.VectorClock = VectorClock;
    }

    public List<Integer> getVectorClock() {
        return VectorClock;
    }

    public void setVectorClock(List<Integer> vectorClock) {
        VectorClock = vectorClock;
    }

    @Override
    public String toString() {
        return "MapMessage{" + "VectorCLock" + VectorClock +
        "} " + super.toString();
    }
}

