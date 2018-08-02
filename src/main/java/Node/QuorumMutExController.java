package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.Queue;
import java.util.UUID;

@Controller
@Slf4j
public class QuorumMutExController {
    private final QuorumMutExService quorumMutExService;
    private final FifoController fifoController;
    private final ThisNodeInfo thisNodeInfo;
    private final QuorumMutExInfo quorumMutExInfo;
    private final CsRequesterInfo csRequesterInfo;
    private final Queue<QuorumMutExInputWork> inputWorkQueue;

    @Autowired
    public QuorumMutExController(
            QuorumMutExService quorumMutExService,
            FifoController fifoController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/csRequester")
            CsRequesterInfo csRequesterInfo,
            @Qualifier("Node/QuorumMutExConfig/inputWorkQueue")
            Queue<QuorumMutExInputWork> inputWorkQueue
            ){
        this.quorumMutExService = quorumMutExService;
        this.fifoController = fifoController;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;
        this.csRequesterInfo = csRequesterInfo;
        this.inputWorkQueue = inputWorkQueue;
    }

    @MessageMapping("/requestMessage")
    public void requestMessage(RequestMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received request message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        final int sourceUid = message.getSourceUID();
        final int sourceScalarClock = message.getSourceScalarClock();
        final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
        final UUID requestId = message.getRequestId();
        final Runnable intakeRequestCall = () -> {
            quorumMutExService.intakeRequest(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
            fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
        };
        final QuorumMutExInputWork work = new QuorumMutExInputWork(intakeRequestCall, sourceScalarClock, sourceCriticalSectionNumber);
        inputWorkQueue.add(work);
    }

    @MessageMapping("/releaseMessage")
    public void releaseMessage(ReleaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received release message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        final int sourceUid = message.getSourceUID();
        final int sourceScalarClock = message.getSourceScalarClock();
        final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
        final UUID requestId = message.getRequestId();
        final Runnable processReleaseCall = () -> {
            quorumMutExService.processRelease(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
            fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
        };
        final QuorumMutExInputWork work = new QuorumMutExInputWork(processReleaseCall, sourceScalarClock, sourceCriticalSectionNumber);
        inputWorkQueue.add(work);
    }

    @MessageMapping("/failedMessage")
    public void failedMessage(FailedMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received failed message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received failed message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            final int sourceUid = message.getSourceUID();
            final int sourceScalarClock = message.getSourceScalarClock();
            final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            final UUID requestId = message.getRequestId();
            final Runnable processFailedCall = () -> {
                quorumMutExService.processFailed(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            final QuorumMutExInputWork work = new QuorumMutExInputWork(processFailedCall, sourceScalarClock, sourceCriticalSectionNumber);
            inputWorkQueue.add(work);
        }
    }

    @MessageMapping("/grantMessage")
    public void grantMessage(GrantMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
               log.trace("<---received grant message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received grant message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            final int sourceUid = message.getSourceUID();
            final int sourceScalarClock = message.getSourceScalarClock();
            final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            final UUID requestId = message.getRequestId();
            final Runnable processGrantCall = () -> {
                quorumMutExService.processGrant(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            final QuorumMutExInputWork work = new QuorumMutExInputWork(processGrantCall, sourceScalarClock, sourceCriticalSectionNumber);
            inputWorkQueue.add(work);
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
            final int sourceUid = message.getSourceUID();
            final int sourceScalarClock = message.getSourceScalarClock();
            final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            final UUID requestId = message.getRequestId();
            final Runnable processInquireCall = () -> {
                quorumMutExService.processInquire(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, requestId);
                fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            final QuorumMutExInputWork work = new QuorumMutExInputWork(processInquireCall, sourceScalarClock, sourceCriticalSectionNumber);
            inputWorkQueue.add(work);
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
//                log.debug("<---received yield message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            final int sourceUid = message.getSourceUID();
            final int sourceScalarClock = message.getSourceScalarClock();
            final int sourceCriticalSectionNumber = message.getSourceCriticalSectionNumber();
            final UUID sourceRequestId = message.getRequestId();
            final Runnable processYieldCall = () -> {
                quorumMutExService.processYield(sourceUid, sourceScalarClock, sourceCriticalSectionNumber, sourceRequestId);
                fifoController.sendFifoResponse(sourceUid, sourceScalarClock, sourceCriticalSectionNumber);
            };
            final QuorumMutExInputWork work = new QuorumMutExInputWork(processYieldCall, sourceScalarClock, sourceCriticalSectionNumber);
            inputWorkQueue.add(work);
        }
    }

    public void sendRequestMessage(int thisNodeUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final RequestMessage message = new RequestMessage(
                thisNodeUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/requestMessage";
        fifoController.sendFifo(message, route);
    }

    public void sendReleaseMessage(int thisNodeUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final ReleaseMessage message = new ReleaseMessage(
                thisNodeUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/releaseMessage";
        fifoController.sendFifo(message, route);
    }

    public void sendGrantMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final GrantMessage message = new GrantMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/grantMessage";
        fifoController.sendFifo(message, route);
    }

    public void sendFailedMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final FailedMessage message = new FailedMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/failedMessage";
        fifoController.sendFifo(message, route);
    }

    public void sendInquireMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final InquireMessage message = new InquireMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/inquireMessage";
        fifoController.sendFifo(message, route);
    }

    public void sendYieldMessage(int thisNodeUid, int targetUid, int scalarClock, int criticalSectionNumber, UUID requestId) throws MessagingException {
        final YieldMessage message = new YieldMessage(
                thisNodeUid,
                targetUid,
                scalarClock,
                criticalSectionNumber,
                requestId
        );
        final String route = "/topic/yieldMessage";
        fifoController.sendFifo(message, route);
    }
}