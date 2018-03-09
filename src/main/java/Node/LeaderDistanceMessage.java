package Node;

public class LeaderDistanceMessage extends NodeRoundSynchronizableMessage {
    private int distance;
    private boolean isNullMessage;

    public LeaderDistanceMessage() {
        super();
    }

    //null message constructor
    public LeaderDistanceMessage(int sourceUID, int roundNumber) {
        super(sourceUID, roundNumber);
        distance = Integer.MAX_VALUE;
        isNullMessage = true;
    }

    public LeaderDistanceMessage(int sourceUID, int roundNumber, int distance) {
        super(sourceUID, roundNumber);
        this.distance = distance;

        isNullMessage = false;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isNullMessage() {
        return isNullMessage;
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
