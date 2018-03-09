package Node;

public class LeaderDistanceMessage extends NodeRoundSynchronizableMessage {
    private int distance;

    public LeaderDistanceMessage() {
        super();
    }

    public LeaderDistanceMessage(int sourceUID, int roundNumber, int distance) {
        super(sourceUID, roundNumber);
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "LeaderAnnounceMessage{" +
                "distance=" + distance +
                "} " + super.toString();
    }
}
