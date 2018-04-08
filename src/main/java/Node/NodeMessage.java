package Node;

public abstract class NodeMessage {
    private int sourceUID;
    private int phaseNumber;
    public NodeMessage(){}

    public NodeMessage(int sourceUID, int phaseNumber){
        this.setSourceUID(sourceUID);
        this.setPhaseNumber(phaseNumber);
    }

    public int getSourceUID() {
        return sourceUID;
    }

    public void setSourceUID(int sourceUID) {
        this.sourceUID = sourceUID;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }

    public void setPhaseNumber(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    @Override
    public String toString() {
        return "NodeMessage{" +
                "sourceUID=" + sourceUID +
                '}';
    }
}