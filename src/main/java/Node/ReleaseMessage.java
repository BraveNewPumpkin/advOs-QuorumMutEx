package Node;

public class ReleaseMessage extends NodeMessage {
    private int criticalSectionNumber;

    public ReleaseMessage() {
    }

    public ReleaseMessage(int sourceUID, int criticalSectionNumber) {
        super(sourceUID);
        this.criticalSectionNumber = criticalSectionNumber;
    }

    public int getCriticalSectionNumber() {
        return criticalSectionNumber;
    }

    public void setCriticalSectionNumber(int criticalSectionNumber) {
        this.criticalSectionNumber = criticalSectionNumber;
    }
}

