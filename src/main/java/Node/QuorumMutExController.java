package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class QuorumMutExController {
    private final QuorumMutExService quorumMutExService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final QuorumMutExInfo quorumMutExInfo;
    private final CsRequesterInfo csRequesterInfo;

    @Autowired
    public QuorumMutExController(
            QuorumMutExService quorumMutExService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
            QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/csRequester")
            CsRequesterInfo csRequesterInfo
            ){
        this.quorumMutExService = quorumMutExService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;
        this.csRequesterInfo = csRequesterInfo;
    }

    @MessageMapping("/requestMessage")
    public void requestMessage(RequestMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received request message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        Runnable intakeRequestCall = () -> {
            int sourceUid = message.getSourceUID();
            int sourceTimestamp = message.getSourceTimestamp();
            quorumMutExService.intakeRequest(sourceUid, sourceTimestamp);
        };
        Thread intakeRequestThread = new Thread(intakeRequestCall);
        intakeRequestThread.start();
    }

    @MessageMapping("/releaseMessage")
    public void releaseMessage(ReleaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received release message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
        Runnable processReleaseCall = () -> {
            int sourceUid = message.getSourceUID();
            int sourceCriticalSectionNumber = message.getCriticalSectionNumber();
            quorumMutExService.processRelease(sourceUid, sourceCriticalSectionNumber);
        };
        Thread processReleaseThread = new Thread(processReleaseCall);
        processReleaseThread.start();
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
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
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
            Runnable processGrantCall = () -> {
                int sourceUid = message.getSourceUID();
                quorumMutExService.processGrant(sourceUid, message.getCriticalSectionNumber());
            };
            Thread processGrantThread = new Thread(processGrantCall);
            processGrantThread.start();
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
            Runnable processInquireCall = () -> {
                int sourceUid = message.getSourceUID();
                quorumMutExService.processInquire(sourceUid, message.getSourceTimeStamp());
            };
            Thread processInquireThread = new Thread(processInquireCall);
            processInquireThread.start();
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
            Runnable processYieldCall = () -> {
                int sourceUid = message.getSourceUID();
                quorumMutExService.processYield(sourceUid);
            };
            Thread processYieldThread = new Thread(processYieldCall);
            processYieldThread.start();
        }
    }

    public void sendRequestMessage() throws MessagingException {
        RequestMessage message = new RequestMessage(
                thisNodeInfo.getUid(),
                quorumMutExInfo.getScalarClock()
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending request message: {}", message);
        }
        template.convertAndSend("/topic/requestMessage", message);
        log.trace("RequestMessage message sent");
    }

    public void sendReleaseMessage() throws MessagingException {
        ReleaseMessage message = new ReleaseMessage(
                thisNodeInfo.getUid(),
                csRequesterInfo.getCriticalSectionNumber()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending release message: {}", message);
        }
        template.convertAndSend("/topic/releaseMessage", message);
        log.trace("ReleaseMessage message sent");
    }

    public void sendGrantMessage(int targetUid) throws MessagingException {
        GrantMessage message = new GrantMessage(
                thisNodeInfo.getUid(),
                targetUid,
                csRequesterInfo.getCriticalSectionNumber()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending grant message: {}", message);
        }
        template.convertAndSend("/topic/grantMessage", message);
        log.trace("GrantMessage message sent");
    }

    public void sendFailedMessage(int targetUid) throws MessagingException {
        FailedMessage message = new FailedMessage(
                thisNodeInfo.getUid(),
                targetUid
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending failed message: {}", message);
        }
        template.convertAndSend("/topic/failedMessage", message);
        log.trace("FailedMessage message sent");
    }

    public void sendInquireMessage(int targetUid, int timeStamp) throws MessagingException {
        InquireMessage message = new InquireMessage(
                thisNodeInfo.getUid(),
                targetUid,
                timeStamp
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending inquire message: {}", message);
        }
        template.convertAndSend("/topic/inquireMessage", message);
        log.trace("InquireMessage message sent");
    }

    public void sendYieldMessage(int targetUid) throws MessagingException {
        YieldMessage message = new YieldMessage(
                thisNodeInfo.getUid(),
                targetUid
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending yield message: {}", message);
        }
        template.convertAndSend("/topic/yieldMessage", message);
        log.trace("YieldMessage message sent");
    }
}