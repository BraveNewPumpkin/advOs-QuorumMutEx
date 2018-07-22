package Node;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class QuorumMutExInfo {
    private int scalarClock;
    private final PriorityQueue<CsRequest> waitingRequestQueue;
    private final Queue<Integer> inquiriesPending;
    private CsRequest activeRequest;
    private boolean isLocked;
    private boolean isInquireSent;
    private boolean isFailedReceived;
    private int grantsReceived;

    public QuorumMutExInfo() {
        this.scalarClock = 0;
        waitingRequestQueue = new PriorityQueue<>();
        inquiriesPending = new LinkedList<>();
        isLocked = false;
        isInquireSent = false;
        isFailedReceived = false;
        grantsReceived = 0;
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

    public boolean isInquireSent() {
        return isInquireSent;
    }

    public void setInquireSent(boolean inquireSent) {
        isInquireSent = inquireSent;
    }

    public Queue<Integer> getInquiriesPending() {
        return inquiriesPending;
    }

    public boolean isFailedReceived() {
        return isFailedReceived;
    }

    public void setFailedReceived(boolean failedReceived) {
        isFailedReceived = failedReceived;
    }

    public int getGrantsReceived() {
        return grantsReceived;
    }

    public void incrementGrantsReceived() {
        grantsReceived++;
    }

    public void decrementGrantsReceived() {
        grantsReceived--;
    }

    public void resetGrantsReceived(){
        grantsReceived=0;
    }
}
