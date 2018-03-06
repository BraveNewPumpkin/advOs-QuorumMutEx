package Node;

public class LeaderAnnounceMessage extends NodeMessage {
    private int leaderUid;
    private int distance;

    public LeaderAnnounceMessage() {
        super();
    }

    public LeaderAnnounceMessage(int sourceUID, int leaderUid, int distance) {
        super(sourceUID);
        this.leaderUid = leaderUid;
        this.distance = distance;
    }

    public int getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(int leaderUid) {
        this.leaderUid = leaderUid;
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
                "leaderUid=" + leaderUid +
                "distance=" + distance +
                "} " + super.toString();
    }
}
