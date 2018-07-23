package Node;

public class ReleaseMessage extends NodeMessage {
    public ReleaseMessage() {
    }

    public ReleaseMessage(int sourceUID, int scalarClock, int criticalSectionNumber) {
        super(sourceUID, scalarClock, criticalSectionNumber);
    }

    @Override
    public String toString() {
        return "ReleaseMessage{} " + super.toString();
    }
}

