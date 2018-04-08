package Node;

public class MwoeResponseMessage extends NodeMessage implements TargetableMessage<Integer> {
    private Integer target;

    public MwoeResponseMessage() {
    }

    public MwoeResponseMessage(int sourceUID, Integer target) {
        super(sourceUID);
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
