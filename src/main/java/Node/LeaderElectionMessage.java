package Node;

public final class LeaderElectionMessage extends NodeRoundSynchronizableMessage {
    private int maxUidSeen;
    private int maxDistanceSeen;

    public LeaderElectionMessage() {
        super();
    }

    public LeaderElectionMessage(int sourceUID, int roundNumber, int maxUidSeen, int maxDistanceSeen) {
        super(sourceUID, roundNumber);
        this.maxUidSeen = maxUidSeen;
        this.maxDistanceSeen = maxDistanceSeen;
    }

    @Override
    public String toString() {
        return "LeaderElectionMessage{" +
                "maxUidSeen=" + maxUidSeen +
                ", maxDistanceSeen=" + maxDistanceSeen +
                "} " + super.toString();
    }

    public int getMaxUidSeen() {
        return maxUidSeen;
    }

    public void setMaxUidSeen(int maxUidSeen) {
        this.maxUidSeen = maxUidSeen;
    }

    public int getMaxDistanceSeen() {
        return maxDistanceSeen;
    }

    public void setMaxDistanceSeen(int maxDistanceSeen) {
        this.maxDistanceSeen = maxDistanceSeen;
    }


}