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
    private Object maxNumberSynchronizer;

    @Autowired
    public MapController(
            MapService mapService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo
            ){
        this.mapService = mapService;
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
                log.debug("<---received map message {}", message);
            }
        }
        synchronized (maxNumberSynchronizer) {
            //TODO
            //check maxNumber
            //set active
            //pick random number of messages to send between minPerActive and maxPerActive
            //loop send message(s)
                //check max number
                //increment current number sent
                //wait minSendDelay
            //set passive
        }
    }

    public void sendMapMessage(int targetUid) throws MessagingException {
        MapMessage message = new MapMessage(
                thisNodeInfo.getUid(),
                targetUid
                );
        if(log.isDebugEnabled()){
            log.debug("--->sending map message: {}", message);
        }
        template.convertAndSend("/topic/mapMessage", message);
        log.trace("MapMessage message sent");
    }
}