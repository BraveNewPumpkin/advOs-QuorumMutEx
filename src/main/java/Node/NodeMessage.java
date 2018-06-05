package Node;

public abstract class NodeMessage {
    private int sourceUID;
    public NodeMessage(){}

    public NodeMessage(int sourceUID){
        this.setSourceUID(sourceUID);
    }

    public int getSourceUID() {
        return sourceUID;
    }

    public void setSourceUID(int sourceUID) {
        this.sourceUID = sourceUID;
    }

    @Override
    public String toString() {
        return "NodeMessage{" +
                "sourceUID=" + sourceUID +
                '}';
    }
}