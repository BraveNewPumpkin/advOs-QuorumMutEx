package Node;

public abstract class SimpleTargetableMessage extends NodeMessage implements TargetableMessage<Integer> {
    int target;

    public SimpleTargetableMessage() {
    }

    public SimpleTargetableMessage(int sourceUID, int phaseNumber, int target) {
        super(sourceUID, phaseNumber);
        this.target = target;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }
}
