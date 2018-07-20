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
    @Autowired
    public QuorumMutExController(
            QuorumMutExService quorumMutExService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/QuorumMutExConfig/quorumMutExInfo")
            QuorumMutExInfo quorumMutExInfo
            ){
        this.quorumMutExService = quorumMutExService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;
    }

    @MessageMapping("/requestMessage")
    public void requestMessage(RequestMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received request message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
    }

    @MessageMapping("/releaseMessage")
    public void releaseMessage(ReleaseMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received release message {}", message);
        }
        //spawn in separate thread to allow the message processing thread to return to threadpool
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
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
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
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
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
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
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
        }
    }

    public void sendRequestMessage() throws MessagingException {
        RequestMessage message = new RequestMessage(
                thisNodeInfo.getUid()
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending request message: {}", message);
        }
        template.convertAndSend("/topic/requestMessage", message);
        log.trace("RequestMessage message sent");
    }

    public void sendReleaseMessage() throws MessagingException {
        ReleaseMessage message = new ReleaseMessage(
                thisNodeInfo.getUid()
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
                targetUid
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

    public void sendInquireMessage(int targetUid) throws MessagingException {
        InquireMessage message = new InquireMessage(
                thisNodeInfo.getUid(),
                targetUid
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