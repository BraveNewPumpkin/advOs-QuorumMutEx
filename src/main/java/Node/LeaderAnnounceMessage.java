package Node;

public class LeaderAnnounceMessage extends NodeMessage {
    private int leaderUid;
    private int maxDistanceFromLeader;

    public LeaderAnnounceMessage() {
        super();
    }

    public LeaderAnnounceMessage(int sourceUID, int leaderUid, int maxDistanceFromLeader) {
        super(sourceUID);
        this.leaderUid = leaderUid;
        this.maxDistanceFromLeader = maxDistanceFromLeader;
    }

    public int getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(int leaderUid) {
        this.leaderUid = leaderUid;
    }

    public int getMaxDistanceFromLeader() {
        return maxDistanceFromLeader;
    }

    public void setMaxDistanceFromLeader(int maxDistanceFromLeader) {
        this.maxDistanceFromLeader = maxDistanceFromLeader;
    }

    @Override
    public String toString() {
        return "LeaderAnnounceMessage{" +
                "leaderUid=" + leaderUid +
                ", maxDistanceFromLeader=" + maxDistanceFromLeader +
                "} " + super.toString();
    }
}
