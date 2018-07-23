package Node;

public abstract class NodeMessage {
    private int sourceUID;
    private int sourceScalarClock;
    private int sourceCriticalSectionNumber;

    public NodeMessage(){}

    public NodeMessage(int sourceUID, int sourceScalarClock, int sourceCriticalSectionNumber){
        this.setSourceUID(sourceUID);
        this.sourceScalarClock = sourceScalarClock;
        this.sourceCriticalSectionNumber = sourceCriticalSectionNumber;
    }

    public int getSourceUID() {
        return sourceUID;
    }

    public void setSourceUID(int sourceUID) {
        this.sourceUID = sourceUID;
    }

    public int getSourceScalarClock() {
        return sourceScalarClock;
    }

    public void setSourceScalarClock(int sourceScalarClock) {
        this.sourceScalarClock = sourceScalarClock;
    }

    public int getSourceCriticalSectionNumber() {
        return sourceCriticalSectionNumber;
    }

    public void setSourceCriticalSectionNumber(int sourceCriticalSectionNumber) {
        this.sourceCriticalSectionNumber = sourceCriticalSectionNumber;
    }
}