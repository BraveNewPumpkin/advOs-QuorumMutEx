package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

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
        quorumMutExController.sendRequestMessage(
            thisNodeInfo.getUid(),
            quorumMutExInfo.getScalarClock(),
            csRequesterInfo.getCriticalSectionNumber()
        );
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
            quorumMutExController.sendReleaseMessage(
                thisNodeInfo.getUid(),
                quorumMutExInfo.getScalarClock(),
                csRequesterInfo.getCriticalSectionNumber()
            );
        }
    }

    public void intakeRequest(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing request sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            CsRequest newRequest = new CsRequest(sourceUid, sourceScalarClock);
            CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            //check if we have active
            log.trace("in intake request");
            if (quorumMutExInfo.isLocked()) {
                log.trace("is locked");
                int newRequestCompareActiveResult = newRequest.compareTo(activeRequest);
                boolean isNewRequestSmallerThanActive = newRequestCompareActiveResult < 0;
                if (isNewRequestSmallerThanActive) {
                    log.trace("new request had smaller timestamp than active {}", activeRequest.toString());
                    //prevent duplicate inquires to same active with boolean
                    if (!quorumMutExInfo.isInquireSent()) {
                        log.trace("have not yet sent inquire");
                        quorumMutExController.sendInquireMessage(
                            thisNodeInfo.getUid(),
                            activeRequest.getSourceUid(),
                            activeRequest.getSourceTimestamp(),
                            csRequesterInfo.getCriticalSectionNumber()
                        );
                        quorumMutExInfo.setInquireSent(true);
                    } else {
                        log.trace("already sent inquire so not sending.");
                    }
                    if(requestQueue.size() > 0) {
                        CsRequest headOfQueue = quorumMutExInfo.getWaitingRequestQueue().element();
                        int newRequestCompareHeadOfQueueResult = newRequest.compareTo(activeRequest);
                        boolean isNewRequestSmallerThanHeadOfQueue = newRequestCompareHeadOfQueueResult < 0;
                        if (isNewRequestSmallerThanHeadOfQueue) {
                            log.trace("new request had smaller timestamp than head of queue {}", headOfQueue.toString());
                            quorumMutExController.sendFailedMessage(
                                    thisNodeInfo.getUid(),
                                    headOfQueue.getSourceUid(),
                                    quorumMutExInfo.getScalarClock(),
                                    csRequesterInfo.getCriticalSectionNumber()
                            );
                        }
                    }
                } else {
                    log.trace("new request had larger or equal timestamp than active {}", activeRequest.toString());
                    quorumMutExController.sendFailedMessage(
                        thisNodeInfo.getUid(),
                        sourceUid,
                        quorumMutExInfo.getScalarClock(),
                        csRequesterInfo.getCriticalSectionNumber()
                    );
                }
                requestQueue.add(newRequest);
            } else {
                log.trace("is not locked");
                quorumMutExInfo.setActiveRequest(newRequest);
                quorumMutExInfo.setLocked(true);
                quorumMutExController.sendGrantMessage(
                    thisNodeInfo.getUid(),
                    sourceUid,
                    quorumMutExInfo.getScalarClock(),
                    csRequesterInfo.getCriticalSectionNumber()
                );
            }
        }
    }

    public void processRelease(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing release sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
            quorumMutExInfo.setInquireSent(false);
            //set new active to first request from queue
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
            if(activeRequest.getSourceUid() == sourceUid) {
                //the release was for the active request
                if (requestQueue.size() > 0) {
                    CsRequest nextActiveRequest = requestQueue.remove();
                    quorumMutExInfo.setActiveRequest(nextActiveRequest);
                    quorumMutExController.sendGrantMessage(
                            thisNodeInfo.getUid(),
                            nextActiveRequest.getSourceUid(),
                            quorumMutExInfo.getScalarClock(),
                            csRequesterInfo.getCriticalSectionNumber()
                    );
                } else {
                    quorumMutExInfo.setLocked(false);
                }
            } else {
                //the release was not for active request so check the queue
                Predicate<CsRequest> doesRequestMatchRelease = (csRequest) -> {
                    return csRequest.getSourceUid() == sourceUid;
                };
                boolean isRequestRemoved = requestQueue.removeIf(doesRequestMatchRelease);
                if (!isRequestRemoved) {
                    log.trace("tried to release for request which was not in queue.");
                }
                //TODO should we send grant?
            }
        }
    }

    public void processFailed(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing failed sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            quorumMutExInfo.setFailedReceived(true);
            quorumMutExInfo.getInquiriesPending().parallelStream().forEach((inquiry) -> {
                //check to make sure this is not an outdated message
                if (inquiry.getSourceCriticalSectionNumber() == csRequesterInfo.getCriticalSectionNumber()) {
                    quorumMutExController.sendYieldMessage(
                        thisNodeInfo.getUid(),
                        inquiry.getSourceUid(),
                        quorumMutExInfo.getScalarClock(),
                        csRequesterInfo.getCriticalSectionNumber()
                    );
                } else {
                    log.trace("ignoring stale inquiry: {}", inquiry);
                }
            });
        }
    }

    public void processGrant(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing grant sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
            quorumMutExInfo.incrementGrantsReceived();
            //check if we have all grants and can run critical section
            if (checkAllGrantsReceived()) {
                criticalSectionLock.release();
            }
        }
    }

    public void processInquire(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing inquire sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            //check if currently in critical section
            if (criticalSectionLock.hasQueuedThreads()) {
                if (quorumMutExInfo.isFailedReceived()) {
                    //check to make sure this is not an outdated message
                    int thisNodeScalarClock = quorumMutExInfo.getScalarClock();
                    if (sourceScalarClock == thisNodeScalarClock) {
                        quorumMutExController.sendYieldMessage(
                                thisNodeInfo.getUid(),
                                sourceUid,
                                thisNodeScalarClock,
                                csRequesterInfo.getCriticalSectionNumber()
                        );
                        quorumMutExInfo.decrementGrantsReceived();
                    } else {
                        if(log.isTraceEnabled()) {
                            log.trace("inquiry's timestamp was not equal to current; ignoring. inquiryTimestamp: {}  current timestamp: {}", sourceScalarClock, quorumMutExInfo.getScalarClock());
                        }
                    }
                } else {
                    ReceivedInquiry inquiry = new ReceivedInquiry(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
                    quorumMutExInfo.getInquiriesPending().add(inquiry);
                }
            }
        }
    }

    public void processYield(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing yield sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}", sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            quorumMutExInfo.setInquireSent(false);
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            if(requestQueue.size() > 0) {
                //swap active and head of queue
                CsRequest headOfQueue = requestQueue.remove();
                requestQueue.add(quorumMutExInfo.getActiveRequest());
                quorumMutExInfo.setActiveRequest(headOfQueue);

                //send grant to active
                quorumMutExController.sendGrantMessage(
                        thisNodeInfo.getUid(),
                        quorumMutExInfo.getActiveRequest().getSourceUid(),
                        quorumMutExInfo.getScalarClock(),
                        csRequesterInfo.getCriticalSectionNumber()
                        );
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