package Node;

public class MwoeRejectMessage extends NodeMessage implements TargetableMessage<Integer> {
    private Integer target;
    public MwoeRejectMessage() {
    }

    public MwoeRejectMessage(int sourceUID, int phaseNumber,  Integer target) {
        super(sourceUID,phaseNumber);
        this.target = target;

    }

    @Override
    public Integer getTarget() {
        return target;
    }

    @Override
    public void setTarget(Integer target) {
        this.target = target;
    }
}
