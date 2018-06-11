package Node;

public class BuildTreeQueryMessage extends NodeMessage {
    public BuildTreeQueryMessage() {
    }

    public BuildTreeQueryMessage(int sourceUID) {
        super(sourceUID);
    }

    @Override
    public String toString() {
        return "BuildTreeQueryMessage{" +
                "} " + super.toString();
    }
}
