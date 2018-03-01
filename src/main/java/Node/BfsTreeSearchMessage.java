package Node;

public final class BfsTreeSearchMessage extends NodeMessage {
    private int distance;

    public BfsTreeSearchMessage() {
        super();
    }

    public BfsTreeSearchMessage(int sourceUID, int distance) {
        super(sourceUID);
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