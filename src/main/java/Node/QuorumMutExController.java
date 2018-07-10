package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.concurrent.Semaphore;

@Controller
@Slf4j
public class QuorumMutExController {
    private final QuorumMutExService quorumMutExService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;

    @Autowired
    public QuorumMutExController(
            QuorumMutExService quorumMutExService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo
            ){
        this.quorumMutExService = quorumMutExService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
    }

    @MessageMapping("/mapMessage")
    public void mapMessage(MapMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
               log.trace("<---received  map message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received map message {}  current Scalar Clock {}", message, thisNodeInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
            activeThingsThread.start();
            sendFifoResponse(message.getSourceUID(), message.getFifoRequestId());
        }
    }


    @MessageMapping("/mapResponseMessage")
    public void receiveFifoResponseMessage(FifoResponseMessage message) {
        if(thisNodeInfo.getUid() == message.getTarget()) {
            if (log.isDebugEnabled()) {
                log.debug("<---received mapResponseMessage {}", message);
            }
            FifoRequestId currentFifoRequestId = fifoResponseRoundSynchronizer.getRoundId();
            if (message.getFifoRequestId().equals(currentFifoRequestId)) {
                sendingFifoSynchronizer.release();
            } else {
                throw new Error("got response with FifoRequestId " + message.getFifoRequestIdAsString() + " did not match current FifoRequestId " + currentFifoRequestId.getRequestId());
            }
        }
    }

    public void sendFifoResponse(int targetUid, FifoRequestId fifoRequestId) throws MessagingException {
        FifoResponseMessage message = new FifoResponseMessage(
                thisNodeInfo.getUid(),
                targetUid,
                fifoRequestId
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending mapResponseMessage: {}", message);
        }
        template.convertAndSend("/topic/mapResponseMessage", message);
        log.trace("mapResponseMessage message sent");
    }

    public void sendMapMessage(int targetUid) throws MessagingException {
        try {
            sendingFifoSynchronizer.acquire();
        } catch (java.lang.InterruptedException e) {
            //ignore
        }
        thisNodeInfo.incrementVectorClock();
        FifoRequestId currentFifoRequestId = new FifoRequestId(thisNodeInfo.getUid()+"map" + snapshotInfo.getSentMessages());
        fifoResponseRoundSynchronizer.setRoundId(currentFifoRequestId);
        MapMessage message = new MapMessage(
                thisNodeInfo.getUid(),
                targetUid,
                thisNodeInfo.getVectorClock(),
                currentFifoRequestId
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending map message: {}", message);
        }
        template.convertAndSend("/topic/mapMessage", message);
        snapshotInfo.incrementSentMessages();
        log.trace("MapMessage message sent");
    }
}