package Node;

public class LeaderAnnounceMessage extends NodeMessage {
    private int leaderUid;

    public LeaderAnnounceMessage() {
        super();
    }

    public LeaderAnnounceMessage(int sourceUID, int leaderUid) {
        super(sourceUID);
        this.leaderUid = leaderUid;
    }

    public int getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(int leaderUid) {
        this.leaderUid = leaderUid;
    }

    @Override
    public String toString() {
        return "LeaderAnnounceMessage{" +
                "leaderUid=" + leaderUid +
                "} " + super.toString();
    }
}
