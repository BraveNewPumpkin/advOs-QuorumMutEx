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
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(BuildTreeAckMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received buildTreeAckMessage message {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received buildTreeAckMessage message {}", message);
            }

        }
    }

    public void sendMarkMessage() throws MessagingException {
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
}
