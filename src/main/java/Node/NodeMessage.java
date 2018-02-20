package Node;

public abstract class NodeMessage {

    private int sourceUID;
    private int targetUID;

    public NodeMessage(int sourceUID, int targetUID){
        this.setSourceUID(sourceUID);
        this.setTargetUID(targetUID);
    }
    public int getSourceUID() {
        return sourceUID;
    }

    public void setSourceUID(int sourceUID) {
        this.sourceUID = sourceUID;
    }

    public int getTargetUID() {
        return targetUID;
    }

    public void setTargetUID(int targetUID) {
        this.targetUID = targetUID;
    }
}