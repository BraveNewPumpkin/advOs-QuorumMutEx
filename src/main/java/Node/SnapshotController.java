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
    private NodeMessageRoundSynchronizer<MarkMessage> snapshotSynchronizer;

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
            TreeInfo treeInfo,
            @Qualifier("Node/SnapshotConfig/snaphshotSynchronizer")
            NodeMessageRoundSynchronizer<MarkMessage> snapshotSynchronizer
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.snapshotSynchronizer = snapshotSynchronizer;

        markedSynchronizer = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {
            if (log.isDebugEnabled()) {
                log.debug("<---received MarkMessage {}", message);
            }
            snapshotSynchronizer.enqueueAndRunIfReady(message, snapshotService::doMarkingThings);
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
                snapshotSynchronizer.getRoundNumber()
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
                snapshotInfo,
                snapshotSynchronizer.getRoundNumber()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending StateMessage: {}", message);
        }
        template.convertAndSend("/topic/stateMessage", message);
        log.trace("StateMessage message sent");
    }
}
