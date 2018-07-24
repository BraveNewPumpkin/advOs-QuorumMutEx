package Node;

public class QuorumMutExWork  implements Comparable<QuorumMutExWork> {
    private final Runnable work;
    private final int scalarClock;
    private final int criticalSectionNumber;

    public QuorumMutExWork(Runnable work, int scalarClock, int criticalSectionNumber) {
        this.work = work;
        this.scalarClock = scalarClock;
        this.criticalSectionNumber = criticalSectionNumber;
    }

    public Runnable getWork() {
        return work;
    }

    public int getScalarClock() {
        return scalarClock;
    }

    public int getCriticalSectionNumber() {
        return criticalSectionNumber;
    }

    @Override
    public int compareTo(QuorumMutExWork o) {
        int thisScalarClock = getCriticalSectionNumber();
        int otherScalarClock = o.getCriticalSectionNumber();
        if(thisScalarClock < otherScalarClock) {
            return -1;
        } else if(thisScalarClock == otherScalarClock) {
            return 0;
        } else {
            return 1;
        }
    }
}
