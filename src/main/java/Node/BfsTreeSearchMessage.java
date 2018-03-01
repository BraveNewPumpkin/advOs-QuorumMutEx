package Node;

public final class BfsTreeSearchMessage extends NodeRoundSynchronizableMessage {
    private int distance;

    public BfsTreeSearchMessage() {
        super();
    }

    public BfsTreeSearchMessage(int sourceUID, int round, int distance) {
        super(sourceUID, round);
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "BfsTreeSearchMessage{" +
                "distance=" + distance +
                "} " + super.toString();
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}