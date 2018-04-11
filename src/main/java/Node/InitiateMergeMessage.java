package Node;

public class InitiateMergeMessage extends SimpleTargetableMessage {
    public InitiateMergeMessage() {
    }

    public InitiateMergeMessage(int sourceUID, int phaseNumber, int target) {
        super(sourceUID, phaseNumber, target);
    }
}
