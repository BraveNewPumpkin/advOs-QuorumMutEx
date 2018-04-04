package Node;

public class MwoeResponseMessage extends NodeMessage implements TargetableMessage<NodeInfo> {
    private NodeInfo target;

    public MwoeResponseMessage() {
    }

    public MwoeResponseMessage(int sourceUID, NodeInfo target) {
        super(sourceUID);
        this.target = target;
    }

    @Override
    public NodeInfo getTarget() {
        return target;
    }

    @Override
    public void setTarget(NodeInfo target) {
        this.target = target;
    }

}
