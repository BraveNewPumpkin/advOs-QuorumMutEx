package Node;

import java.util.PriorityQueue;

public class QuorumMutExInfo {
    private int scalarClock;
    private final PriorityQueue<CsRequest> waitingRequestQueue;
    private CsRequest activeRequest;
    private boolean isLocked;

    public QuorumMutExInfo() {
        this.scalarClock = 0;
        waitingRequestQueue = new PriorityQueue<>();
        isLocked = false;
    }

    public int getScalarClock () {
                               return scalarClock;
                                                  }

    public void setScalarClock ( int scalarClock){
        this.scalarClock = scalarClock;
    }

    public void incrementScalarClock () {
        scalarClock++;
    }

    public PriorityQueue<CsRequest> getWaitingRequestQueue() {
        return waitingRequestQueue;
    }

    public void setActiveRequest(CsRequest activeRequest) {
        this.activeRequest = activeRequest;
    }

    public CsRequest getActiveRequest() {
        return activeRequest;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
