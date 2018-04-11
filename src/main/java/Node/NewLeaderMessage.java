package Node;

public class NewLeaderMessage extends SimpleTargetableMessage {
    public NewLeaderMessage() {
    }

    public NewLeaderMessage(int sourceUID, int phaseNumber, int target) {
        super(sourceUID, phaseNumber, target);
    }
}
