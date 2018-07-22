package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QuorumMutExService {
    private final QuorumMutExController quorumMutExController;
    private final QuorumMutExInfo quorumMutExInfo;
    private final CsRequesterInfo csRequesterInfo;
    private final ThisNodeInfo thisNodeInfo;

    private final Semaphore criticalSectionLock;

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
        quorumMutExInfo.incrementScalarClock();
        quorumMutExInfo.setFailedReceived(false);
        quorumMutExInfo.getInquiriesPending().clear();
        quorumMutExInfo.resetGrantsReceived();
        quorumMutExController.sendReleaseMessage();
    }

    public synchronized void intakeRequest(int sourceUid, int sourceClock) {
        CsRequest newRequest = new CsRequest(sourceUid, sourceClock);
        CsRequest activeRequest = quorumMutExInfo.getActiveRequest();
        //check if we have active
        if(quorumMutExInfo.isLocked()) {
            int compareResult = newRequest.compareTo(activeRequest);
            if(compareResult < 0) {
                //prevent duplicate inquires to same active with boolean
                if(!quorumMutExInfo.isInquireSent()) {
                    quorumMutExController.sendInquireMessage(activeRequest.getSourceUid(), activeRequest.getSourceTimestamp());
                    quorumMutExInfo.setInquireSent(true);
                }
            } else {
                quorumMutExController.sendFailedMessage(sourceUid);
            }
            quorumMutExInfo.getWaitingRequestQueue().add(newRequest);
        } else {
            quorumMutExInfo.setActiveRequest(newRequest);
            quorumMutExInfo.setLocked(true);
            quorumMutExController.sendGrantMessage(sourceUid);
        }
    }

    public void processRelease(int sourceUid, int sourceCriticalSectionNumber) {
        csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
        quorumMutExInfo.setInquireSent(false);
        //set new active to first request from queue
        Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
        if(requestQueue.size() > 0) {
            CsRequest nextActiveRequest = requestQueue.remove();
            quorumMutExInfo.setActiveRequest(nextActiveRequest);
            quorumMutExController.sendGrantMessage(nextActiveRequest.getSourceUid());
        } else {
            quorumMutExInfo.setLocked(false);
        }
    }

    public void processFailed(int sourceUid) {
        quorumMutExInfo.setFailedReceived(true);
        quorumMutExInfo.getInquiriesPending().parallelStream().forEach((uid) -> {
            quorumMutExController.sendYieldMessage(sourceUid);
        });
    }

    public void processGrant(int sourceUid, int sourceCriticalSectionNumber) {
        csRequesterInfo.mergeCriticalSectionNumber(sourceCriticalSectionNumber);
        quorumMutExInfo.incrementGrantsReceived();
        //check if we have all grants and can run critical section
        if(checkAllGrantsReceived()) {
            criticalSectionLock.release();
        }
    }

    public void processInquire(int sourceUid, int sourceTimeStamp) {
        //check to make sure this is not an outdated message
        if(sourceTimeStamp == quorumMutExInfo.getScalarClock()) {
            //check if currently in critical section
            if(!criticalSectionLock.hasQueuedThreads()) {
                if (quorumMutExInfo.isFailedReceived()) {
                    quorumMutExController.sendYieldMessage(sourceUid);
                    quorumMutExInfo.decrementGrantsReceived();
                } else {
                    quorumMutExInfo.getInquiriesPending().add(sourceUid);
                }
            }
        }
    }

    public void processYield(int sourceUid) {
        quorumMutExInfo.setInquireSent(false);
        quorumMutExInfo.setLocked(false);
        //swap active and head of queue
        Queue<CsRequest> requestQueue = quorumMutExInfo.getWaitingRequestQueue();
        CsRequest headOfQueue = requestQueue.remove();
        requestQueue.add(quorumMutExInfo.getActiveRequest());
        quorumMutExInfo.setActiveRequest(headOfQueue);
        //send grant to active
        quorumMutExController.sendGrantMessage(quorumMutExInfo.getActiveRequest().getSourceUid());
    }

    public boolean checkAllGrantsReceived() {
        int numGrantsReceived = quorumMutExInfo.getGrantsReceived();
        int quorumSize = thisNodeInfo.getQuorum().size();
        return numGrantsReceived == quorumSize;
    }
}