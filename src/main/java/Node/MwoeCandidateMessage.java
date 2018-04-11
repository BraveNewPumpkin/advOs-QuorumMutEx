package Node;

public class MwoeCandidateMessage extends NodeRoundSynchronizableMessage implements TargetableMessage<Integer> {
    private Integer target;
    public Edge mwoeCandidate;

    public MwoeCandidateMessage() {
    }

    public MwoeCandidateMessage(int sourceUID, int phaseNumber, Integer target, Edge mwoeCandidate) {
        super(sourceUID, phaseNumber, phaseNumber);
        this.target = target;
        this.mwoeCandidate = mwoeCandidate;
    }

    @Override
    public Integer getTarget() {
        return target;
    }

    @Override
    public void setTarget(Integer target) {
        this.target = target;
    }

    public Edge getMwoeCandidate(){
        return mwoeCandidate;
    }

    public void setMwoeCandidate(Edge mwoeCandidate) {
        this.mwoeCandidate = mwoeCandidate;
    }

    @Override
    public String toString() {
        return "MwoeCandidateMessage{" +
                "target=" + target +
                ", mwoeCandidate=" + mwoeCandidate +
                "} " + super.toString();
    }
}
