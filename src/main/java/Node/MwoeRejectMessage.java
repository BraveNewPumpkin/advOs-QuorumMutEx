package Node;

public class MwoeRejectMessage implements TargetableMessage<Integer> {
    private Integer target;
    public MwoeRejectMessage() {

    }

    public MwoeRejectMessage(Integer target) {
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
