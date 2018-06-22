package Node;

public class RoundSynchronizableMessage extends NodeMessage implements RoundSynchronizable {
    private int roundNumber;

    public RoundSynchronizableMessage() {
        super();
    }

    public RoundSynchronizableMessage(int sourceUID, int roundNumber) {
        super(sourceUID);
        this.roundNumber = roundNumber;
    }

    @Override
    public String toString() {
        return "RoundSynchronizableMessage{" +
                "roundNumber=" + roundNumber +
                "} " + super.toString();
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }
}
