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
    private final TreeInfo treeInfo;

    //used to prevent race conditions with checking if marked
    private Object markedSynchronizer;

    @Autowired
    public SnapshotController(
            SnapshotService snapshotService,
            SimpMessagingTemplate template,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo")
            ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/snapshotInfo")
            SnapshotInfo snapshotInfo,
            @Qualifier("Node/BuildTreeConfig/treeInfo")
            TreeInfo treeInfo
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;

        markedSynchronizer = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {
        synchronized (markedSynchronizer) {
            //TODO if mark is for future snapshot, buffer it
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

    @MessageMapping("/stateMessage")
    public void receiveStateMessage(StateMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received StateMessage {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received StateMessage {}", message);
            }
            snapshotService.doStateThings();
        }
    }

    public void sendMarkMessage() throws MessagingException {
        MarkMessage message = new MarkMessage(
                thisNodeInfo.getUid(),
                snapshotInfo.getSnapshotNumber()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending MarkMessage: {}", message);
        }
        template.convertAndSend("/topic/markMessage", message);
        log.trace("MarkMessage message sent");
    }

    public void sendStateMessage() throws MessagingException {
        StateMessage message = new StateMessage(
                thisNodeInfo.getUid(),
                treeInfo.getParentId(),
                snapshotInfo
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending StateMessage: {}", message);
        }
        template.convertAndSend("/topic/stateMessage", message);
        log.trace("StateMessage message sent");
    }
}
