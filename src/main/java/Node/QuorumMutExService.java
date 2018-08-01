package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.UUID;
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
    private UUID thisNodeCsRequestId;

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
        UUID requestId = generateUuid();

        //wait until we are allowed to enter cs
        quorumMutExController.sendRequestMessage(
            thisNodeInfo.getUid(),
            quorumMutExInfo.getScalarClock(),
            csRequesterInfo.getCriticalSectionNumber(),
            requestId
        );

        thisNodeCsRequestId = requestId;

        try {
            criticalSectionLock.acquire();
        } catch(java.lang.InterruptedException e) {
            //ignore
        }
    }

    public void cs_leave() {
        synchronized (messageProcessingSynchronizer) {
            csRequesterInfo.incrementCriticalSectionNumber();
            quorumMutExInfo.incrementScalarClock();
            quorumMutExInfo.setFailedReceived(false);
            quorumMutExInfo.getInquiriesPendingFailed().clear();
            quorumMutExInfo.resetGrantsReceived(thisNodeCsRequestId);
            quorumMutExController.sendReleaseMessage(
                    thisNodeInfo.getUid(),
                    quorumMutExInfo.getScalarClock(),
                    csRequesterInfo.getCriticalSectionNumber() - 1,
                    thisNodeCsRequestId
            );
        }
    }

    public void intakeRequest(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing request sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {} requestId: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber,
                    requestId
            );
            CsRequest newRequest = new CsRequest(sourceUid, sourceScalarClock, requestId);
            CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            //check if we have active
            log.trace("in intake request");
            if (quorumMutExInfo.isActive()) {
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
                                csRequesterInfo.getCriticalSectionNumber(),
                                activeRequest.getRequestId()
                        );
                        quorumMutExInfo.setInquireSent(true);
                    } else {
                        log.trace("already sent inquire so not sending.");
                    }
                    if(requestQueue.size() > 0) {
                        CsRequest headOfQueue = quorumMutExInfo.getWaitingRequestQueue().element();
                        int newRequestCompareHeadOfQueueResult = newRequest.compareTo(headOfQueue);
                        boolean isNewRequestSmallerThanHeadOfQueue = newRequestCompareHeadOfQueueResult < 0;
                        if (isNewRequestSmallerThanHeadOfQueue) {
                            log.trace("new request had smaller timestamp than head of queue {}", headOfQueue.toString());
                            quorumMutExController.sendFailedMessage(
                                    thisNodeInfo.getUid(),
                                    headOfQueue.getSourceUid(),
                                    quorumMutExInfo.getScalarClock(),
                                    csRequesterInfo.getCriticalSectionNumber(),
                                    headOfQueue.getRequestId()
                            );
                        } else {
                            log.trace("new request had larger timestamp than head of queue {}", headOfQueue.toString());
                            quorumMutExController.sendFailedMessage(
                                    thisNodeInfo.getUid(),
                                    sourceUid,
                                    quorumMutExInfo.getScalarClock(),
                                    csRequesterInfo.getCriticalSectionNumber(),
                                    requestId
                            );
                        }
                    }
                } else {
                    log.trace("new request had larger or equal timestamp than active {}", activeRequest.toString());
                    quorumMutExController.sendFailedMessage(
                            thisNodeInfo.getUid(),
                            sourceUid,
                            quorumMutExInfo.getScalarClock(),
                            csRequesterInfo.getCriticalSectionNumber(),
                            requestId
                    );
                }
                requestQueue.add(newRequest);
            } else {
                log.trace("is not locked");
                quorumMutExInfo.setActiveRequest(newRequest);
                quorumMutExInfo.setActive(true);
                quorumMutExController.sendGrantMessage(
                        thisNodeInfo.getUid(),
                        sourceUid,
                        quorumMutExInfo.getScalarClock(),
                        csRequesterInfo.getCriticalSectionNumber(),
                        requestId
                );
            }
        }
    }

    public void processRelease(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing release sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {} requestId: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber,
                    requestId
            );
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber + 1);
            quorumMutExInfo.setInquireSent(false);
            //set new active to first request from queue
            Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
            UUID activeRequestId = quorumMutExInfo.getActiveRequest().getRequestId();
            if(activeRequestId.equals(requestId)) {
                //the release was for the active request
                if (requestQueue.size() > 0) {
                    CsRequest nextActiveRequest = requestQueue.remove();
                    quorumMutExInfo.setActiveRequest(nextActiveRequest);
                    quorumMutExController.sendGrantMessage(
                            thisNodeInfo.getUid(),
                            nextActiveRequest.getSourceUid(),
                            quorumMutExInfo.getScalarClock(),
                            csRequesterInfo.getCriticalSectionNumber(),
                            quorumMutExInfo.getActiveRequest().getRequestId()
                    );
                } else {
                    quorumMutExInfo.setActive(false);
                }
            } else {
                log.trace("active requestId: {} != release requestId: {}", activeRequestId, requestId);
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

    public void processFailed(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing failed sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {} requestId: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber,
                    requestId
            );
            quorumMutExInfo.setFailedReceived(true);
            CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
            quorumMutExInfo.getInquiriesPendingFailed().forEach((inquiry) -> {
                //check to make sure this is not an outdated message
                UUID inquiryRequestId = inquiry.getRequestId();
                if (inquiryRequestId.equals(thisNodeCsRequestId)) {
                    int inquirySourceUid = inquiry.getSourceUid();
                    quorumMutExController.sendYieldMessage(
                        thisNodeInfo.getUid(),
                        inquirySourceUid,
                        quorumMutExInfo.getScalarClock(),
                        csRequesterInfo.getCriticalSectionNumber()
                    );
                    if(quorumMutExInfo.isGrantReceived(inquiryRequestId, inquirySourceUid)) {
                        quorumMutExInfo.removeGrantReceived(inquiryRequestId, inquirySourceUid);
                        if(log.isTraceEnabled()) {
                            log.trace("grants: {} -> {} of {} for request: {}",
                                    quorumMutExInfo.getNumGrantsReceived(inquiryRequestId) + 1,
                                    quorumMutExInfo.getNumGrantsReceived(inquiryRequestId),
                                    thisNodeInfo.getQuorum().size(),
                                    inquiryRequestId
                            );
                        }
                    } else {
                        log.error("tried to remove grant that we didn't have from {}", inquirySourceUid);
                    }
                } else {
                    log.error("ignoring mismatched inquiry: {}", inquiry);
                }
            });
        }
    }

    public void processGrant(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing grant sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {} requestId: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber,
                    requestId
            );
            csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
            quorumMutExInfo.addGrantReceived(requestId, sourceUid);
            Queue<ReceivedInquiry> pendingInquiries = quorumMutExInfo.getInquiriesPendingGrant();
            pendingInquiries.forEach((inquiry) -> {
                if(inquiry.getRequestId().equals(requestId)){
                    renameMe(
                            inquiry.getSourceUid(),
                            inquiry.getSourceTimeStamp(),
                            inquiry.getSourceCriticalSectionNumber(),
                            inquiry.getRequestId()
                    );
                }
            });
            if(log.isTraceEnabled()) {
                log.trace("grants: {} -> {} of {}",
                        quorumMutExInfo.getNumGrantsReceived(requestId) - 1,
                        quorumMutExInfo.getNumGrantsReceived(requestId),
                        thisNodeInfo.getQuorum().size()
                );
            }
            //check if we have all grants and can run critical section
            if (checkAllGrantsReceived(requestId)) {
                criticalSectionLock.release();
            }
        }
    }

    public void processInquire(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing inquire sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {} requestId: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber,
                    requestId
            );
            //check if currently in critical section
            if (criticalSectionLock.hasQueuedThreads()) {
                CsRequest activeRequest = quorumMutExInfo.getActiveRequest();

                //Received an inquire when the process is not locked. Add it to inquiries pending
                if(quorumMutExInfo.isActive()) {
                    //check that the inquire matches a current grant otherwise enqueue
                    boolean hasMatchingGrant = quorumMutExInfo.isGrantReceived(requestId, sourceUid);
                    if(hasMatchingGrant) {
                        renameMe(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                    } else {
                        ReceivedInquiry inquiry = new ReceivedInquiry(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                        quorumMutExInfo.getInquiriesPendingGrant().add(inquiry);
                    }
                } else {
                    ReceivedInquiry inquiry = new ReceivedInquiry(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                    quorumMutExInfo.getInquiriesPendingFailed().add(inquiry);
                }
            }
        }
    }

    public void renameMe(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber, UUID requestId) {
        if (quorumMutExInfo.isFailedReceived()) {
            //check to make sure this is not an outdated message
            int thisNodeScalarClock = quorumMutExInfo.getScalarClock();
            int thisNodeCriticalSectionNumber = csRequesterInfo.getCriticalSectionNumber();
            if (requestId.equals(thisNodeCsRequestId)) {
                quorumMutExController.sendYieldMessage(
                        thisNodeInfo.getUid(),
                        sourceUid,
                        thisNodeScalarClock,
                        thisNodeCriticalSectionNumber
                );
                quorumMutExInfo.removeGrantReceived(requestId, sourceUid);
                if (log.isTraceEnabled()) {
                    log.trace("grants: {} -> {} of {}",
                            quorumMutExInfo.getNumGrantsReceived(requestId) + 1,
                            quorumMutExInfo.getNumGrantsReceived(requestId),
                            thisNodeInfo.getQuorum().size());
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.error("inquiry's requestId {} was not equal to current sent request {}; ignoring.", requestId, thisNodeCsRequestId);
                }
            }
        } else {
            ReceivedInquiry inquiry = new ReceivedInquiry(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
            quorumMutExInfo.getInquiriesPendingFailed().add(inquiry);
        }

    }

    public void processYield(int sourceUid, int sourceScalarClock, int sourceCriticalSectionNumber) {
        synchronized (messageProcessingSynchronizer) {
            log.trace("processing yield sourceUid: {} sourceScalarClock: {} sourceCriticalSectionNumber: {}",
                    sourceUid,
                    sourceScalarClock,
                    sourceCriticalSectionNumber
            );
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
                        csRequesterInfo.getCriticalSectionNumber(),
                        quorumMutExInfo.getActiveRequest().getRequestId()
                        );
            } else {
                log.trace("processing yield from {}, but request queue was empty.", sourceUid);
//                quorumMutExInfo.setActive(false);
            }
        }
    }

    public boolean checkAllGrantsReceived(UUID requestId) {
        int numGrantsReceived = quorumMutExInfo.getNumGrantsReceived(requestId);
        int quorumSize = thisNodeInfo.getQuorum().size();
        return numGrantsReceived == quorumSize;
    }

    private UUID generateUuid() {
        return UUID.randomUUID();
    }
}