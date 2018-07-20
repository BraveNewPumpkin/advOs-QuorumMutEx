package Node;

public class GrantMessage extends SimpleTargetableMessage {
    public GrantMessage() {
    }

    public GrantMessage(int sourceUID, int target) {
        super(sourceUID, target);
    }
}

