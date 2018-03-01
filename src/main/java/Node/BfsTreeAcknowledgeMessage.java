package Node;

public class BfsTreeAcknowledgeMessage extends NodeMessage {
    private int targetUid;
    private int distance;

    public BfsTreeAcknowledgeMessage() {
    }

    public BfsTreeAcknowledgeMessage(int sourceUID, int targetUid, int distance) {
        super(sourceUID);
        this.targetUid = targetUid;
        this.distance = distance;
    }

    public int getTargetUid() {
        return targetUid;
    }

    public void setTargetUid(int targetUid) {
        this.targetUid = targetUid;
    }
}
