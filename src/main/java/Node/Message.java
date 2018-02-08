package Node;

public abstract class Message {

    private int sourceUID;
    private int targetUID;

    public Message(int sourceUID, int targetUID){
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