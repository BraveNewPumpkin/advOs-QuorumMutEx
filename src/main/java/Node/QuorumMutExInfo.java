package Node;

import java.util.*;

public class QuorumMutExInfo {
    private int scalarClock;
    private final PriorityQueue<CsRequest> waitingRequestQueue;
    private final Queue<ReceivedInquiry> inquiriesPending;
    private CsRequest activeRequest;
    private boolean isLocked;
    private boolean isInquireSent;
    private boolean isFailedReceived;
    private Set<Integer> grantsReceived;

    public QuorumMutExInfo() {
        this.scalarClock = 0;
        waitingRequestQueue = new PriorityQueue<>();
        inquiriesPending = new LinkedList<ReceivedInquiry>();
        isLocked = false;
        isInquireSent = false;
        isFailedReceived = false;
        grantsReceived = new HashSet<Integer>();
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

    public Queue<ReceivedInquiry> getInquiriesPending() {
        return inquiriesPending;
    }

    public boolean isFailedReceived() {
        return isFailedReceived;
    }

    public void setFailedReceived(boolean failedReceived) {
        isFailedReceived = failedReceived;
    }

    public int getNumGrantsReceived() {
        return grantsReceived.size();
    }

    public boolean isGrantReceived(int grantorUid) {
        return grantsReceived.contains(grantorUid);
    }

    public void addGrantReceived(int grantorUid) {
        grantsReceived.add(grantorUid);
    }

    public void removeGrantReceived(int grantorUid) {
        grantsReceived.remove(grantorUid);
    }

    public void resetGrantsReceived() {
        grantsReceived.clear();
    }
}
