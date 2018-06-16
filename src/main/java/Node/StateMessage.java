package Node;

public class StateMessage extends SimpleTargetableMessage {
    private SnapshotInfo snapshotInfo;
    public StateMessage() {
    }

    public StateMessage(int sourceUID, int target, SnapshotInfo snapshotInfo) {
        super(sourceUID, target);
        this.snapshotInfo = snapshotInfo;
    }

}
