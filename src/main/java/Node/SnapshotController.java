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

    //used to prevent race conditions with checking if marked
    private Object markedSynchronizer;

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
    public void receiveMarkMessage(MarkMessage message) {
        synchronized (markedSynchronizer) {
            if (snapshotService.isMarked()) {
                if (log.isTraceEnabled()) {
                    log.trace("<---received MarkMessage {}", message);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("<---received MarkMessage {}", message);
                }
                snapshotService.doMarkingThings();
            }
        }
    }

    public void sendMarkMessage() throws MessagingException {
        MarkMessage message = new MarkMessage(
                thisNodeInfo.getUid()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending MarkMessage: {}", message);
        }
        template.convertAndSend("/topic/markMessage", message);
        log.trace("MarkMessage message sent");
    }
}
