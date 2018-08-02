package Node;

public abstract class QuorumMutExWork {
    private final int scalarClock;
    private final int criticalSectionNumber;

    public QuorumMutExWork(int scalarClock, int criticalSectionNumber) {
        this.scalarClock = scalarClock;
        this.criticalSectionNumber = criticalSectionNumber;
    }

    public int getScalarClock() {
        return scalarClock;
    }

    public int getCriticalSectionNumber() {
        return criticalSectionNumber;
    }
}
