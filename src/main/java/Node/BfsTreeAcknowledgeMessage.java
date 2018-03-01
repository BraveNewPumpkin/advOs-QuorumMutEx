package Node;

public class BfsTreeAcknowledgeMessage extends NodeMessage {
    private int targetUid;
    private int distanceToRoot;
    private Tree<Integer> tree;

    public BfsTreeAcknowledgeMessage() {
    }

    public BfsTreeAcknowledgeMessage(int sourceUID, int targetUid, int distanceToRoot, Tree<Integer> tree) {
        super(sourceUID);
        this.targetUid = targetUid;
        this.distanceToRoot = distanceToRoot;
        this.tree = tree;
    }

    public int getTargetUid() {
        return targetUid;
    }

    public void setTargetUid(int targetUid) {
        this.targetUid = targetUid;
    }
}
