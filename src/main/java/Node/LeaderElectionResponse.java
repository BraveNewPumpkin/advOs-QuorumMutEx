package Node;

public final class LeaderElectionResponse extends NodeMessage {
    final String header = "CONNECTED";

    public LeaderElectionResponse(int sourceUID, int targetUID) {
        super(sourceUID, targetUID);
    }
}