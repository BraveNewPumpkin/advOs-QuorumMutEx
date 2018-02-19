package Node;

public final class LeaderElectionMessage extends Message {
    public LeaderElectionMessage(int sourceUID, int targetUID) {
        super(sourceUID, targetUID);
    }
}