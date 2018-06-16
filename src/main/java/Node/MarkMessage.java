package Node;

public class MarkMessage extends NodeMessage {
    public MarkMessage() {
    }

    public MarkMessage(int sourceUID) {
        super(sourceUID);
    }

    @Override
    public String toString() {
        return "MarkMessage{} " + super.toString();
    }
}
