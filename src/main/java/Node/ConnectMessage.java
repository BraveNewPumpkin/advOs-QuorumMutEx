package Node;

public final class ConnectMessage extends Message {
    final String header = "CONNECT";

    public ConnectMessage(int sourceUID, int targetUID) {
        super(sourceUID, targetUID);
    }
}