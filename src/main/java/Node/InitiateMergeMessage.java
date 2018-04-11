package Node;

public class InitiateMergeMessage extends SimpleTargetableMessage {
    Edge mwoeEdge;
    int componentId;
    public InitiateMergeMessage() {
    }

    public InitiateMergeMessage(int sourceUID, int phaseNumber, int target, Edge mwoeEdge, int componentId) {
        super(sourceUID, phaseNumber, target);
        this.mwoeEdge = mwoeEdge;
        this.componentId = componentId;
    }

    public Edge getMwoeEdge() {
        return mwoeEdge;
    }

    public void setMwoeEdge(Edge mwoeEdge) {
        this.mwoeEdge = mwoeEdge;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    @Override
    public String toString() {
        return "InitiateMergeMessage{" +
                "mwoeEdge=" + mwoeEdge +
                ", componentId=" + componentId +
                "} " + super.toString();
    }
}
