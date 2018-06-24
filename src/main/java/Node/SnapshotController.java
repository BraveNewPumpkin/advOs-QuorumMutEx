package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

@Controller
@Slf4j
public class SnapshotController {
    private final SnapshotService snapshotService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private final NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private final NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer;

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
            @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
            NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer,
            @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
            NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;

        markedSynchronizer = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {
            if (log.isDebugEnabled()) {
                log.debug("<---received MarkMessage {}. {} of {} this round", message, snapshotMarkerSynchronizer.getNumMessagesThisRound() + 1, snapshotMarkerSynchronizer.getRoundSize());
            }

            snapshotService.checkAndSendMarkerMessage(message.getRoundNumber());

            snapshotMarkerSynchronizer.enqueueAndRunIfReady(message, snapshotService::doMarkingThings);
    }

    @MessageMapping("/stateMessage")
    public void receiveStateMessage(StateMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isTraceEnabled()) {
                log.trace("<---received StateMessage {}", message);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("<---received StateMessage {}. {} of {} this round", message, snapshotStateSynchronizer.getNumMessagesThisRound() + 1, snapshotStateSynchronizer.getRoundSize());
            }
            Runnable doStateThings = () -> {
                Map<Integer, SnapshotInfo> snapshotInfoMap = new HashMap<>();
                Queue<StateMessage> messages = snapshotStateSynchronizer.getMessagesThisRound();
                messages.forEach((StateMessage stateMessage) -> {
                    snapshotInfoMap.putAll(stateMessage.getSnapshotInfos());
                });
                snapshotService.doStateThings(snapshotInfoMap, message.getSnapshotNumber());
            };
            snapshotStateSynchronizer.enqueueAndRunIfReady(message, doStateThings);
        }
    }

    public void sendMarkMessage(int roundNumber) throws MessagingException {
        MarkMessage message = new MarkMessage(
                thisNodeInfo.getUid(),
                roundNumber
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending MarkMessage: {}", message);
        }
        template.convertAndSend("/topic/markMessage", message);
        log.trace("MarkMessage message sent");
    }

    public void sendStateMessage(Map<Integer, SnapshotInfo> snapshotInfos) throws MessagingException {
        StateMessage message = new StateMessage(
                thisNodeInfo.getUid(),
                treeInfo.getParentId(),
                snapshotInfos,
                snapshotStateSynchronizer.getRoundNumber()
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending StateMessage: {}", message);
        }
        template.convertAndSend("/topic/stateMessage", message);
        log.trace("StateMessage message sent");
    }
}
