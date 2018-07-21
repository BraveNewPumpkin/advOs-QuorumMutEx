package Node;

public class CsRequesterInfo {
    private final int interRequestDelay;
    private final int csExecutionTime;
    private int criticalSectionNumber;

    public CsRequesterInfo(int interRequestDelay, int csExecutionTime) {
        this.interRequestDelay = interRequestDelay;
        this.csExecutionTime = csExecutionTime;

        criticalSectionNumber = 0;
    }

    public int getInterRequestDelay() {
        return interRequestDelay;
    }

    public int getCsExecutionTime() {
        return csExecutionTime;
    }

    public int getCriticalSectionNumber() {
        return criticalSectionNumber;
    }

    public void setCriticalSectionNumber(int criticalSectionNumber) {
        this.criticalSectionNumber = criticalSectionNumber;
    }

    public void incrementCriticalSectionNumber() {
        criticalSectionNumber++;
    }

    public void mergeCriticalSectionNumber (int criticalSectionNumber) {
        int maxValue = Math.max(this.criticalSectionNumber, criticalSectionNumber);
        this.criticalSectionNumber = maxValue;
    }
}
