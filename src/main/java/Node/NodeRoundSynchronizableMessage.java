package Node;

public class NodeRoundSynchronizableMessage extends NodeMessage implements RoundSynchronizableMessage{
    private int roundNumber;

    public NodeRoundSynchronizableMessage() {
        super();
    }

    public NodeRoundSynchronizableMessage(int sourceUID, int phaseNumber, int roundNumber) {
        super(sourceUID,phaseNumber);
        this.roundNumber = roundNumber;
    }

    @Override
    public String toString() {
        return "NodeRoundSynchronizableMessage{" +
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
