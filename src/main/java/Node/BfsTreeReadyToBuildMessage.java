package Node;

public class BfsTreeReadyToBuildMessage extends NodeMessage{
    public BfsTreeReadyToBuildMessage() {
    }

    public BfsTreeReadyToBuildMessage(int sourceUID) {
        super(sourceUID);
    }

    @Override
    public String toString() {
        return "BfsTreeReadyToBuildMessage{} " + super.toString();
    }
}
