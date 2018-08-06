package Node;

import java.util.*;

public class QuorumMutExInfo {
    private int scalarClock;
    private int numSentMessages;
    private final PriorityQueue<CsRequest> waitingRequestQueue;
    private final Queue<ReceivedInquiry> inquiriesPendingFailed;
    private final Queue<ReceivedInquiry> inquiriesPendingGrant;
    private CsRequest activeRequest;
    private boolean isActive;
    private Map<UUID, Boolean> inquiresSent;
    private Map<UUID, Boolean> failedsReceived;
    private Map<UUID, Set<Integer>> grantsReceived;
    private Set<UUID> lastRelease;

    public QuorumMutExInfo() {
        this.scalarClock = 0;
        waitingRequestQueue = new PriorityQueue<>();
        inquiriesPendingFailed = new LinkedList<ReceivedInquiry>();
        inquiriesPendingGrant = new LinkedList<ReceivedInquiry>();
        isActive = false;
        inquiresSent = new HashMap<>();
        failedsReceived = new HashMap<>();
        grantsReceived = new HashMap<>();

        lastRelease=new HashSet<>();
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

    public int getNumSentMessages() {
        return numSentMessages;
    }

    public void incrementNumSentMessages() {
        numSentMessages++;
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

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isInquireSent(UUID requestId) {
        return inquiresSent.getOrDefault(requestId, false);
    }

    public void setInquireSent(UUID requestId, boolean inquireSent) {
        if(inquireSent == true) {
            inquiresSent.put(requestId, inquireSent);
        } else {
            if(inquiresSent.containsKey(requestId)) {
                inquiresSent.remove(requestId);
            }
        }
    }

    public Queue<ReceivedInquiry> getInquiriesPendingFailed() {
        return inquiriesPendingFailed;
    }

    public Queue<ReceivedInquiry> getInquiriesPendingGrant() {
        return inquiriesPendingGrant;
    }

    public boolean removeInquiryPendingGrant(UUID requestId) {
        return  inquiriesPendingGrant.removeIf((inquiry -> {
            return inquiry.getRequestId().equals(requestId);
        }));
    }

    public boolean removeInquiryPendingGrant(UUID requestId, int sourceUid) {
        return inquiriesPendingGrant.removeIf((inquiry -> {
            return (inquiry.getRequestId().equals(requestId) && inquiry.getSourceUid() == sourceUid);
        }));
    }


        public boolean isFailedReceived(UUID requestId) {
        return failedsReceived.getOrDefault(requestId, false);
    }

    public void setFailedReceived(UUID requestId, boolean failedReceived) {
        if(failedReceived == true) {
            failedsReceived.put(requestId, failedReceived);
        } else {
            if(failedsReceived.containsKey(requestId)) {
                failedsReceived.remove(requestId);
            }
        }
    }

    public int getNumGrantsReceived(UUID requestId) {
        int numGrantsReceived = 0;
        if(grantsReceived.containsKey(requestId)) {
            Set<Integer> grantsReceivedForRequest = grantsReceived.get(requestId);
            numGrantsReceived = grantsReceivedForRequest.size();
        }
        return numGrantsReceived;
    }

    public boolean isGrantReceived(UUID requestId, int grantorUid) {
        boolean isGrantReceived = false;
        if(grantsReceived.containsKey(requestId)) {
            Set<Integer> grantsReceivedForRequest = grantsReceived.get(requestId);
            if(grantsReceivedForRequest.contains(grantorUid)) {
                isGrantReceived = true;
            }
        }
        return isGrantReceived;
    }

    public void addGrantReceived(UUID requestId, int grantorUid) {
        Set<Integer> grantsReceivedForRequest;
        if(grantsReceived.containsKey(requestId)) {
            grantsReceivedForRequest = grantsReceived.get(requestId);
        } else {
            grantsReceivedForRequest = new HashSet<Integer>(1);
        }
        grantsReceivedForRequest.add(grantorUid);
        grantsReceived.put(requestId, grantsReceivedForRequest);
    }

    public void removeGrantReceived(UUID requestId, int grantorUid) {
        Set<Integer> grantsReceivedForRequest = grantsReceived.get(requestId);
        grantsReceivedForRequest.remove(grantorUid);
    }

    public void resetGrantsReceived(UUID requestId) {
        grantsReceived.remove(requestId);
    }

    public int countNumOfFailedReceived(){
        int count=0;
        for(UUID req:failedsReceived.keySet()) {
            if(failedsReceived.get(req)==true)
                count++;
        }
        return count;
    }

    public Set<UUID> getLastRelease() {
        return lastRelease;
    }

    public void setLastRelease(UUID lastRelease) {
        this.lastRelease.add(lastRelease);
    }

    public boolean checkIfReleased(UUID requestID){
        if(this.lastRelease.contains(requestID))
            return true;
        else
            return false;
    }
}
