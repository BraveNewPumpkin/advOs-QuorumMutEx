package Node;

public class GrantMessage extends SimpleTargetableMessage {
    private int criticalSectionNumber;

    public GrantMessage() {
    }

    public GrantMessage(int sourceUID, int target, int criticalSectionNumber) {
        super(sourceUID, target);
        this.criticalSectionNumber = criticalSectionNumber;
    }

    public int getCriticalSectionNumber() {
        return criticalSectionNumber;
    }
    `

    public void setCriticalSectionNumber(int criticalSectionNumber) {
        this.criticalSectionNumber = criticalSectionNumber;
    }
}

