package Node;

public class QuorumMutExInputWork extends QuorumMutExWork implements Comparable<QuorumMutExInputWork> {
    private final Runnable work;

    public QuorumMutExInputWork(Runnable work, int scalarClock, int criticalSectionNumber) {
        super(scalarClock, criticalSectionNumber);
        this.work = work;
    }

    public Runnable getWork() {
        return work;
    }

    @Override
    public int compareTo(QuorumMutExInputWork o) {
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
