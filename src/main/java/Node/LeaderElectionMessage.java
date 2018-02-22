package Node;

public final class LeaderElectionMessage extends NodeMessage {
    private int roundNumber;
    private int maxUidSeen;
    private int maxDistanceSeen;

    public LeaderElectionMessage() {
        super();
    }

    public LeaderElectionMessage(int sourceUID, int targetUID, int roundNumber, int maxUidSeen, int maxDistanceSeen) {
        super(sourceUID, targetUID);
        this.roundNumber = roundNumber;
        this.maxUidSeen = maxUidSeen;
        this.maxDistanceSeen = maxDistanceSeen;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
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