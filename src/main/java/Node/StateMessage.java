package Node;

public class StateMessage extends SimpleTargetableMessage {
    private SnapshotInfo snapshotInfo;
    private int snapshotNumber;

    public StateMessage() {
    }

    public StateMessage(int sourceUID, int target, SnapshotInfo snapshotInfo, int snapshotNumber) {
        super(sourceUID, target);
        this.snapshotInfo = snapshotInfo;
        this.snapshotNumber = snapshotNumber;
    }

    public SnapshotInfo getSnapshotInfo() {
        return snapshotInfo;
    }

    public void setSnapshotInfo(SnapshotInfo snapshotInfo) {
        this.snapshotInfo = snapshotInfo;
    }

    public int getSnapshotNumber() {
        return snapshotNumber;
    }

    public void setSnapshotNumber(int snapshotNumber) {
        this.snapshotNumber = snapshotNumber;
    }
}
