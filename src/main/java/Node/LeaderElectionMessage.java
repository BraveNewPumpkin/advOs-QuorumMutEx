package Node;

public final class LeaderElectionMessage extends NodeMessage {
    public LeaderElectionMessage(int sourceUID, int targetUID) {
        super(sourceUID, targetUID);
    }
}