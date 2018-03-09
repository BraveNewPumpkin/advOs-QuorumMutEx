package Node;

public class BfsTreeAcknowledgeMessage extends NodeMessage {
    private int targetUid;
    private int distanceToRoot;

    public BfsTreeAcknowledgeMessage() {
    }

    public BfsTreeAcknowledgeMessage(int sourceUID, int targetUid, int distanceToRoot) {
        super(sourceUID);
        this.targetUid = targetUid;
        this.distanceToRoot = distanceToRoot;
    }

    public int getTargetUid() {
        return targetUid;
    }

    public void setTargetUid(int targetUid) {
        this.targetUid = targetUid;
    }

    @Override
    public String toString() {
        return "BfsTreeAcknowledgeMessage{" +
                "targetUid=" + targetUid +
                ", distanceToRoot=" + distanceToRoot +
                "} " + super.toString();
    }
}
