package Node;

public class BuildTreeNackMessage extends SimpleTargetableMessage {
    public BuildTreeNackMessage() {
    }

    public BuildTreeNackMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }

    @Override
    public String toString() {
        return "BuildTreeNackMessage{} " + super.toString();
    }

}
