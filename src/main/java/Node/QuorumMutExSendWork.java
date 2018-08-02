package Node;

public class QuorumMutExSendWork<T extends NodeMessage> extends QuorumMutExWork {
    private final T message;
    private final String route;

    public QuorumMutExSendWork(int scalarClock, int criticalSectionNumber, T message, String route) {
        super(scalarClock, criticalSectionNumber);
        this.message = message;
        this.route = route;
    }

    public T getMessage() {
        return message;
    }

    public String getRoute() {
        return route;
    }
}
