package Node;

public final class MwoeSearchMessage extends NodeMessage{
    private int componentId;

    public MwoeSearchMessage() {
        super();
    }

    public MwoeSearchMessage(int sourceUID, int phaseNumber, int componentId) {
        super(sourceUID,phaseNumber);
        this.componentId = componentId;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    @Override
    public String toString() {
        return "MwoeSearchMessage{" +
                "componentId=" + componentId +
                ", phase number="+ getPhaseNumber()+
                "} " + super.toString();
    }
}