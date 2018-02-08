package Node;

public final class ConnectResponse extends Message {
    final String header = "CONNECTED";

    public ConnectResponse(int sourceUID, int targetUID) {
        super(sourceUID, targetUID);
    }
}