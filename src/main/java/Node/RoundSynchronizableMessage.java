package Node;

public class RoundSynchronizableMessage<T> extends NodeMessage implements RoundSynchronizable<T> {
    private T roundId;

    public RoundSynchronizableMessage() {
        super();
    }

    public RoundSynchronizableMessage(int sourceUID, T roundId) {
        super(sourceUID);
        this.roundId = roundId;
    }

    @Override
    public String toString() {
        return "RoundSynchronizableMessage{" +
                "roundId=" + roundId +
                "} " + super.toString();
    }

    @Override
    public T getRoundId() {
        return roundId;
    }

    @Override
    public void setRoundId(T roundId) {
        this.roundId = roundId;
    }
}
