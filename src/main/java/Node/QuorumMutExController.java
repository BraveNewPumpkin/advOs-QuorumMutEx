package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Queue;
import java.util.UUID;

@Controller
@Slf4j
public class QuorumMutExController {
    private final QuorumMutExService quorumMutExService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final QuorumMutExInfo quorumMutExInfo;
    private final CsRequesterInfo csRequesterInfo;
    private final Queue<QuorumMutExWork> workQueue;

    @Autowired
    public QuorumMutExController(
            QuorumMutExService quorumMutExService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/csRequester")
            CsRequesterInfo csRequesterInfo,
            @Qualifier("Node/QuorumMutExConfig/workQueue")
            Queue<QuorumMutExWork> workQueue
            ){
        this.quorumMutExService = quorumMutExService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;
        this.csRequesterInfo = csRequesterInfo;
        this.workQueue = workQueue;
    }

    @MessageMapping("/requestMessage")
    public void requestMessage(RequestMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received request message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        int sourceUid = message.getSourceUID();
        int sourceScalarClock = message.getSourceScalarClock();
        int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
        UUID requestId = message.getRequestId();
        Runnable intakeRequestCall = () -> {
            quorumMutExService.intakeRequest(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
        };
        QuorumMutExWork work = new QuorumMutExWork(intakeRequestCall, sourceScalarClock, sourceCriticalSectionNumber);
        workQueue.add(work);
    }

    @MessageMapping("/releaseMessage")
    public void releaseMessage(ReleaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received release message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        int sourceUid = message.getSourceUID();
        int sourceScalarClock = message.getSourceScalarClock();
        int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
        UUID requestId = message.getRequestId();
        Runnable processReleaseCall = () -> {
            quorumMutExService.processRelease(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
        };
        QuorumMutExWork work = new QuorumMutExWork(processReleaseCall, sourceScalarClock, sourceCriticalSectionNumber);
        workQueue.add(work);
    }

    @MessageMapping("/failedMessage")
    public void failedMessage(FailedMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received  failed message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received failed message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            int sourceUid = message.getSourceUID();
            int sourceScalarClock = message.getSourceScalarClock();
            int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            UUID requestId = message.getRequestId();
            Runnable processFailedCall = () -> {
                quorumMutExService.processFailed(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
            };
            QuorumMutExWork work = new QuorumMutExWork(processFailedCall, sourceScalarClock, sourceCriticalSectionNumber);
            workQueue.add(work);
        }
    }

    @MessageMapping("/grantMessage")
    public void grantMessage(GrantMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
               log.trace("<---received  grant message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received grant message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            int sourceUid = message.getSourceUID();
            int sourceScalarClock = message.getSourceScalarClock();
            int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            UUID requestId = message.getRequestId();
            Runnable processGrantCall = () -> {
                quorumMutExService.processGrant(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
            };
            QuorumMutExWork work = new QuorumMutExWork(processGrantCall, sourceScalarClock, sourceCriticalSectionNumber);
            workQueue.add(work);
        }
    }

    @MessageMapping("/inquireMessage")
    public void inquireMessage(InquireMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received  inquire message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received inquire message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            int sourceUid = message.getSourceUID();
            int sourceScalarClock = message.getSourceScalarClock();
            int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            Runnable processInquireCall = () -> {
                quorumMutExService.processInquire(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            QuorumMutExWork work = new QuorumMutExWork(processInquireCall, sourceScalarClock, sourceCriticalSectionNumber);
            workQueue.add(work);
        }
    }

    @MessageMapping("/yieldMessage")
    public void yieldMessage(YieldMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received  yield message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received yield message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            int sourceUid = message.getSourceUID();
            int sourceScalarClock = message.getSourceScalarClock();
            int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            Runnable processYieldCall = () -> {
                quorumMutExService.processYield(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            QuorumMutExWork work = new QuorumMutExWork(processYieldCall, sourceScalarClock, sourceCriticalSectionNumber);
            workQueue.add(work);
        }
    }

    public void sendRequestMessage(int thisNodeUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        RequestMessage message = new RequestMessage(
                thisNodeUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending request message: {}", message);
        }
        template.convertAndSend("/topic/requestMessage", message);
        log.trace("RequestMessage message sent");
    }

    public void sendReleaseMessage(int thisNodeUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        ReleaseMessage message = new ReleaseMessage(
                thisNodeUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending release message: {}", message);
        }
        template.convertAndSend("/topic/releaseMessage", message);
        log.trace("ReleaseMessage message sent");
    }

    public void sendGrantMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        GrantMessage message = new GrantMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending grant message: {}", message);
        }
        template.convertAndSend("/topic/grantMessage", message);
        log.trace("GrantMessage message sent");
    }

    public void sendFailedMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        FailedMessage message = new FailedMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending failed message: {}", message);
        }
        template.convertAndSend("/topic/failedMessage", message);
        log.trace("FailedMessage message sent");
    }

    public void sendInquireMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber) throws MessagingException {
        InquireMessage message = new InquireMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending inquire message: {}", message);
        }
        template.convertAndSend("/topic/inquireMessage", message);
        log.trace("InquireMessage message sent");
    }

    public void sendYieldMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber) throws MessagingException {
        YieldMessage message = new YieldMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending yield message: {}", message);
        }
        template.convertAndSend("/topic/yieldMessage", message);
        log.trace("YieldMessage message sent");
    }
}