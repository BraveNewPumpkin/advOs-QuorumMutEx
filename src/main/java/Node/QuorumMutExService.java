package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.Semaphore;

@Service
@Slf4j
public class QuorumMutExService {
    private final QuorumMutExController quorumMutExController;
    private final QuorumMutExInfo quorumMutExInfo;
    private final CsRequesterInfo csRequesterInfo;
    private final ThisNodeInfo thisNodeInfo;

    private final Semaphore criticalSectionLock;

    private final Object messageProcessingSynchronizer;

    @Autowired
    public QuorumMutExService(
            @Lazy QuorumMutExController quorumMutExController,
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/csRequester")
            CsRequesterInfo csRequesterInfo,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.quorumMutExController = quorumMutExController;
        this.quorumMutExInfo = quorumMutExInfo;
        this.csRequesterInfo = csRequesterInfo;
        this.thisNodeInfo = thisNodeInfo;

        criticalSectionLock = new Semaphore(0);
        messageProcessingSynchronizer = new Object();
    }

    public void cs_enter() {
        //wait until we are allowed to enter cs
        quorumMutExController.sendRequestMessage();
        try {
            criticalSectionLock.acquire();
        } catch(java.lang.InterruptedException e) {
            //ignore
        }
    }

    public void cs_leave() {
        synchronized (messageProcessingSynchronizer) {
            quorumMutExInfo.incrementScalarClock();
            quorumMutExInfo.setFailedReceived(false);
            quorumMutExInfo.getInquiriesPending().clear();
            quorumMutExInfo.resetGrantsReceived();
            quorumMutExController.sendReleaseMessage();
        }
    }

    public void intakeRequest(int sourceUid, int sourceClock) {
        synchronized (messageProcessingSynchronizer) {
            CsRequest newRequest = new CsRequest(sourceUid, sourceClock);
            CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
            //check if we have active
            log.trace("in intake request");
            if (quorumMutExInfo.isLocked()) {
                log.trace("is locked");
                int compareResult = newRequest.compareTo(activeRequest);
                if (compareResult < 0) {
                    log.trace("new request had smaller timestamp");
                    //prevent duplicate inquires to same active with boolean
                    if (!quorumMutExInfo.isInquireSent()) {
                        log.trace("have not yet sent inquire");
                        quorumMutExController.sendInquireMessage(activeRequest.getSourceUid(), activeRequest.getSourceTimestamp());
                        quorumMutExInfo.setInquireSent(true);
                    } else {
                        log.trace("already sent inquire so not sending.");
                    }
                } else {
                    log.trace("new request had larger or equal timestamp");
                    quorumMutExController.sendFailedMessage(sourceUid);
                }
                quorumMutExInfo.getWaitingRequestQueue().add(newRequest);
            } else {
                log.trace("is not locked");
                quorumMutExInfo.setActiveRequest(newRequest);
                quorumMutExInfo.setLocked(true);
                quorumMutExController.sendGrantMessage(sourceUid);
            }
        }
    }

    public void processRelease(int sourceUid, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
            quorumMutExInfo.setInquireSent(false);
            //set new active to first request from queue
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            if (requestQueue.size() > 0) {
                CsRequest nextActiveRequest = requestQueue.remove();
                quorumMutExInfo.setActiveRequest(nextActiveRequest);
                quorumMutExController.sendGrantMessage(nextActiveRequest.getSourceUid());
            } else {
                quorumMutExInfo.setLocked(false);
            }
        }
    }

    public void processFailed(int sourceUid) {
        synchronized (messageProcessingSynchronizer) {
            quorumMutExInfo.setFailedReceived(true);
            quorumMutExInfo.getInquiriesPending().parallelStream().forEach((inquiry) -> {
                //check to make sure this is not an outdated message
                if (inquiry.getSourceTimeStamp() == quorumMutExInfo.getScalarClock()) {
                    quorumMutExController.sendYieldMessage(inquiry.getSourceUid());
                } else {
                    log.trace("ignoring stale inquiry: {}", inquiry);
                }
            });
        }
    }

    public void processGrant(int sourceUid, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
            quorumMutExInfo.incrementGrantsReceived();
            //check if we have all grants and can run critical section
            if (checkAllGrantsReceived()) {
                criticalSectionLock.release();
            }
        }
    }

    public void processInquire(int sourceUid, int sourceTimeStamp) {
        synchronized (messageProcessingSynchronizer) {
            //check if currently in critical section
            if (criticalSectionLock.hasQueuedThreads()) {
                if (quorumMutExInfo.isFailedReceived()) {
                    //check to make sure this is not an outdated message
                    if (sourceTimeStamp == quorumMutExInfo.getScalarClock()) {
                        quorumMutExController.sendYieldMessage(sourceUid);
                        quorumMutExInfo.decrementGrantsReceived();
                    } else {
                        if(log.isTraceEnabled()) {
                            log.trace("inquiry's timestamp was not equal to current; ignoring. inquiryTimestamp: {}  current timestamp: {}", sourceTimeStamp, quorumMutExInfo.getScalarClock());
                        }
                    }
                } else {
                    ReceivedInquiry inquiry = new ReceivedInquiry(sourceUid, sourceTimeStamp);
                    quorumMutExInfo.getInquiriesPending().add(inquiry);
                }
            }
        }
    }

    public void processYield(int sourceUid) {
        synchronized (messageProcessingSynchronizer) {
            quorumMutExInfo.setInquireSent(false);
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            if(requestQueue.size() > 0) {
                //swap active and head of queue
                CsRequest headOfQueue = requestQueue.remove();
                requestQueue.add(quorumMutExInfo.getActiveRequest());
                quorumMutExInfo.setActiveRequest(headOfQueue);

                //send grant to active
                quorumMutExController.sendGrantMessage(quorumMutExInfo.getActiveRequest().getSourceUid());
            } else {
                log.trace("processing yield from {}, but request queue was empty.", sourceUid);
                quorumMutExInfo.setLocked(false);
            }
        }
    }

    public boolean checkAllGrantsReceived() {
        int numGrantsReceived = quorumMutExInfo.getGrantsReceived();
        int quorumSize = thisNodeInfo.getQuorum().size();
        return numGrantsReceived == quorumSize;
    }
}