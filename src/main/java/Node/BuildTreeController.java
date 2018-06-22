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
public class BuildTreeController {
    private final BuildTreeService buildTreeService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;

    private Object parentIdMutex;

    @Autowired
    public BuildTreeController(
            BuildTreeService buildTreeService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo
    ){
        this.buildTreeService = buildTreeService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;

        parentIdMutex = new Object();
    }

    @MessageMapping("/topic/buildTreeQueryMessage")
    public void receiveBuildTreeQueryMessage(BuildTreeQueryMessage message) {
        synchronized (parentIdMutex) {
            if (buildTreeService.hasParent()) {
                if (log.isTraceEnabled()) {
                    log.trace("<---received BuildTreeQueryMessage message {}", message);
                }
                sendBuildTreeNackMessage(message.getSourceUID());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("<---received BuildTreeQueryMessage message {}", message);
                }
                buildTreeService.setParent(message.getSourceUID());
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
            buildTreeService.doBuildTreeAckThings(message.getSourceUID(), message.getTree());

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
            buildTreeService.doBuildTreeNackThings();
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
        log.trace("BuildTreeQueryMessage message sent");
    }

    public void sendBuildTreeAckMessage(int targetUid) throws MessagingException {
        BuildTreeAckMessage message = new BuildTreeAckMessage(
                thisNodeInfo.getUid(),
                targetUid,
                buildTreeService.genTree()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending build tree ack message: {}", message);
        }
        template.convertAndSend("/topic/buildTreeAckMessage", message);
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
        log.trace("BuildTreeNackMessage message sent");
    }
}
