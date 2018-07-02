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
public class MapController {
    private final MapService mapService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private final SnapshotInfo snapshotInfo;
    private final Semaphore sendingFifoSynchronizer;
    private final MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer;

    @Autowired
    public MapController(
            MapService mapService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo,
            @Qualifier("Node/NodeConfigurator/sendingFifoSynchronizer")
            Semaphore sendingFifoSynchronizer,
            @Qualifier("Node/SnapshotConfig/fifoResponseRoundSynchronizer")
            MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer
            ){
        this.mapService = mapService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.sendingFifoSynchronizer = sendingFifoSynchronizer;
        this.fifoResponseRoundSynchronizer = fifoResponseRoundSynchronizer;
    }

    @MessageMapping("/mapMessage")
    public void mapMessage(MapMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
               log.trace("<---received  map message {}", message);
            }
        } else {
            thisNodeInfo.mergeVectorClock(message.getVectorClock());
            if (log.isDebugEnabled()) {
                log.debug("<---received map message {}  current Vector Clock {}", message, thisNodeInfo.getVectorClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
            Thread activeThingsThread = new Thread(mapService::doActiveThings);
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