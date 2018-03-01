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
public class BfsTreeController {
    private final BfsTreeService bfsTreeService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;


    @Autowired
    public BfsTreeController(
            BfsTreeService bfsTreeService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ){
        this.bfsTreeService = bfsTreeService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
    }


    @MessageMapping("/bfsTreeSearch")
    public void bfsTreeSearch(BfsTreeSearchMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received bfs tree search message {}", message);
        }
        synchronized (this) {
            bfsTreeService.search(message.getSourceUID(), message.getDistance());
        }

        //TODO process message, use service, build tree

    }

    @MessageMapping("/bfsTreeAcknowledge")
    public void bfsTreeAcknowledge(BfsTreeAcknowledgeMessage message) {
        if (log.isDebugEnabled()) {
            log.debug("<---received bfs tree acknowledge message {}", message);
        }
        bfsTreeService.acknowledge(message.getTargetUid());
    }

    public void sendBfsTreeSearch() throws MessagingException {
        BfsTreeSearchMessage message = new BfsTreeSearchMessage(
            thisNodeInfo.getUid(),
            bfsTreeService.getDistanceToNeighborFromRoot()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending BfsTreeSearch message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeSearch", message);
        log.trace("BfsTreeSearch message sent");
    }

    public void sendBfsTreeAcknowledge() throws MessagingException {
        BfsTreeAcknowledgeMessage message = new BfsTreeAcknowledgeMessage(
                thisNodeInfo.getUid(),
                bfsTreeService.getParentUID(),
                bfsTreeService.getThisDistanceFromRoot()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending BfsTreeAcknowledge message: {}", message);
        }
        template.convertAndSend("/topic/bfsTreeAcknowledge", message);
        log.trace("BfsTreeAcknowledge message sent");
    }
}
