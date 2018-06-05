package Node;

public class MapMessage extends SimpleTargetableMessage {
    public MapMessage() {
    }

    public MapMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }

    @Override
    public String toString() {
        return "MapMessage{" +
                "} " + super.toString();
    }
}
