package Node;

public final class MwoeSearchMessage extends NodeRoundSynchronizableMessage{
    private int componentId;
    private boolean isNullMessage;

    public MwoeSearchMessage() {
        super();
    }

    public MwoeSearchMessage(int sourceUID, int phaseNumber, int componentId) {
        super(sourceUID, phaseNumber, phaseNumber);
        this.componentId = componentId;
        isNullMessage = false;
    }

    public MwoeSearchMessage(int sourceUID, int phaseNumber, int componentId, boolean isNullMessage) {
        super(sourceUID, phaseNumber, phaseNumber);
        this.componentId = componentId;
        this.isNullMessage = isNullMessage;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public boolean isNullMessage() {
        return isNullMessage;
    }

    public void setNullMessage(boolean nullMessage) {
        isNullMessage = nullMessage;
    }

    @Override
    public String toString() {
        return "MwoeSearchMessage{" +
                "componentId=" + componentId +
                ", phase number="+ getPhaseNumber()+
                "} " + super.toString();
    }
}