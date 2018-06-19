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
public class MapController {
    private final MapService mapService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;

    @Autowired
    public MapController(
            MapService mapService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo
            ){
        this.mapService = mapService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
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
            mapService.doActiveThings();
        }
    }

    public void sendMapMessage(int targetUid) throws MessagingException {
        thisNodeInfo.incrementVectorClock();
        MapMessage message = new MapMessage(
                thisNodeInfo.getUid(),
                targetUid,
                thisNodeInfo.getVectorClock()
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending map message: {}", message);
        }
        template.convertAndSend("/topic/mapMessage", message);
        snapshotInfo.incrementSentMessages();
        log.trace("MapMessage message sent");
    }
}