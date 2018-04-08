package Node;

public class MwoeResponseMessage extends NodeMessage implements TargetableMessage<Integer> {
    private Integer target;

    public MwoeResponseMessage() {
    }

    public MwoeResponseMessage(int sourceUID, Integer target, int phaseNumber) {
        super(sourceUID, phaseNumber);
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
