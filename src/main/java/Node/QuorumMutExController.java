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

    @MessageMapping("/mapMessage")
    public void mapMessage(MapMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
               log.trace("<---received  map message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received map message {}  current Scalar Clock {}", message, quorumMutExInfo.getScalarClock());
            }
            //spawn in separate thread to allow the message processing thread to return to threadpool
//            Thread activeThingsThread = new Thread(quorumMutExService::doActiveThings);
//            activeThingsThread.start();
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