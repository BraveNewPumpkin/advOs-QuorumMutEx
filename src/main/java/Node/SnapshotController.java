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
public class SnapshotController {
    private final SnapshotService snapshotService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;

    private Object parentIdMutex;

    @Autowired
    public SnapshotController(
            SnapshotService snapshotService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;

        parentIdMutex = new Object();
    }

    @MessageMapping("/topic/buildTreeQueryMessage")
    public void receiveBuildTreeQueryMessage(BuildTreeQueryMessage message) {
        synchronized (parentIdMutex) {
            if (snapshotService.hasParent()) {
                if (log.isTraceEnabled()) {
                    log.trace("<---received  map message {}", message);
                }
                sendBuildTreeNackMessage(message.getSourceUID());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("<---received map message {}", message);
                }
                snapshotService.setParent(message.getSourceUID());
                //send query to all neighbors
                sendBuildTreeQueryMessage();
            }
        }
    }

    @MessageMapping("/buildTreeAckMessage")
    public void receiveBuildTreeAckMessage(BuildTreeAckMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received buildTreeAckMessage message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received buildTreeAckMessage message {}", message);
            }
            snapshotService.doBuildTreeAckThings(message.getSourceUID(), message.getTree());

        }
    }

    @MessageMapping("/buildTreeNackMessage")
    public void receiveBuildTreeNackMessage(BuildTreeNackMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received buildTreeNackMessage message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received buildTreeNackMessage message {}", message);
            }
            snapshotService.doBuildTreeNackThings();
        }
    }

    public void sendBuildTreeQueryMessage() throws MessagingException {
        BuildTreeQueryMessage message = new BuildTreeQueryMessage(
                thisNodeInfo.getUid()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending build tree query message: {}", message);
        }
        template.convertAndSend("/topic/buildTreeQueryMessage", message);
        snapshotInfo.incrementSentMessages();
        log.trace("BuildTreeQueryMessage message sent");
    }

    public void sendBuildTreeAckMessage(int targetUid) throws MessagingException {
        BuildTreeAckMessage message = new BuildTreeAckMessage(
                thisNodeInfo.getUid(),
                targetUid,
                snapshotService.genTree()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending build tree ack message: {}", message);
        }
        template.convertAndSend("/topic/buildTreeAckMessage", message);
        snapshotInfo.incrementSentMessages();
        log.trace("BuildTreeAckMessage message sent");
    }

    public void sendBuildTreeNackMessage(int targetUid) throws MessagingException {
        BuildTreeNackMessage message = new BuildTreeNackMessage(
                thisNodeInfo.getUid(),
                targetUid
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending build tree nack message: {}", message);
        }
        template.convertAndSend("/topic/buildTreeNackMessage", message);
        snapshotInfo.incrementSentMessages();
        log.trace("BuildTreeNackMessage message sent");
    }
}
