package Node;

public class QuorumMutExInfo {

    private int scalarClock;


    public QuorumMutExInfo() {
        this.scalarClock = 0;
    }

        public int getScalarClock () {
            return scalarClock;
        }

        public void setScalarClock ( int scalarClock){
            this.scalarClock = scalarClock;
        }
        public void incrementScalarClock () {
            int incrementedValue = scalarClock++;
            scalarClock = incrementedValue;
        }
        public void mergeScalarCLock () {
            int maxValue = max(scalarClock, csScalarClock);
            scalarClock = maxValue;
            incrementScalarClock();
        }


}
