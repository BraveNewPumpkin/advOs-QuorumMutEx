package Node;

public class LeaderAnnounceMessage extends NodeMessage {
    private int leaderUid;

    public LeaderAnnounceMessage() {
        super();
    }

    public LeaderAnnounceMessage(int sourceUID, int targetUID, int leaderUid) {
        super(sourceUID, targetUID);
        this.leaderUid = leaderUid;
    }

    public int getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(int leaderUid) {
        this.leaderUid = leaderUid;
    }
}
