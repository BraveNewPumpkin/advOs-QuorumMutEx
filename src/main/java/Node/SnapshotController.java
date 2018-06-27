package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;

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
    private Object doingStateOrMarkingThings;

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
        doingStateOrMarkingThings = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {
        Runnable doCallMarkingThingsForMessage = () -> {
            snapshotService.doMarkingThings(message.getRoundNumber());
        };

        //spawn in separate thread to allow the message processing thread to return to threadpool
        Runnable doMarkingThings = () -> {
            synchronized (doingStateOrMarkingThings) {
                if (log.isDebugEnabled()) {
                    log.debug("<---received MarkMessage {}. {} of {} this round", message, snapshotMarkerSynchronizer.getNumMessagesForGivenRound(message.getRoundNumber()) + 1, snapshotMarkerSynchronizer.getRoundSize());
                }

                snapshotService.checkAndSendMarkerMessage(message.getRoundNumber());
                snapshotMarkerSynchronizer.enqueueAndRunIfReadyNotInOrder(message, doCallMarkingThingsForMessage);
            }
        };
        Thread markingThingsThread = new Thread(doMarkingThings);
        markingThingsThread.start();
    }

    @MessageMapping("/stateMessage")
    public void receiveStateMessage(StateMessage message) {
        Runnable doReceiveStateMessageThings = () -> {
            synchronized (doingStateOrMarkingThings) {
                if (thisNodeInfo.getUid() != message.getTarget()) {
                    if (log.isTraceEnabled()) {
                        log.trace("<---received StateMessage {}", message);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("<---received StateMessage {}. {} of {} for round {}", message, snapshotStateSynchronizer.getNumMessagesForGivenRound(message.getSnapshotNumber()) + 1, snapshotStateSynchronizer.getRoundSize(), message.getSnapshotNumber());
                    }
                    Runnable doStateThingsForMessage = () -> {
                        List<Map<Integer, SnapshotInfo>> snapshotInfoMaps = new ArrayList<>();
                        Queue<StateMessage> messages = snapshotStateSynchronizer.getMessagesForGivenRound(message.getSnapshotNumber());
                        messages.forEach((StateMessage stateMessage) -> {
                            snapshotInfoMaps.add(stateMessage.getSnapshotInfos());
                        });
                        snapshotService.doStateThings(snapshotInfoMaps, message.getSnapshotNumber());
                    };
                    Runnable doStateThingsForSubsequent = () -> {
                        List<Map<Integer, SnapshotInfo>> snapshotInfoMaps = new ArrayList<>();
                        Queue<StateMessage> messages = snapshotStateSynchronizer.getMessagesForGivenRound(snapshotStateSynchronizer.getRoundNumber());
                        messages.forEach((StateMessage stateMessage) -> {
                            snapshotInfoMaps.add(stateMessage.getSnapshotInfos());
                        });
                        snapshotService.doStateThings(snapshotInfoMaps, snapshotStateSynchronizer.getRoundNumber());
                    };
                    snapshotStateSynchronizer.enqueueAndRunIfReadyInOrder(message, doStateThingsForMessage);
                    while(snapshotStateSynchronizer.getNumMessagesThisRound() == snapshotStateSynchronizer.getRoundSize()) {
                        snapshotStateSynchronizer.runCurrentRoundIfReady(doStateThingsForSubsequent);
                    }
                }
            }
        };
        Thread stateThingsThread = new Thread(doReceiveStateMessageThings);
        stateThingsThread.start();
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

    public void sendStateMessage(Map<Integer, SnapshotInfo> snapshotInfos, int roundNumber) throws MessagingException {
        StateMessage message = new StateMessage(
                thisNodeInfo.getUid(),
                treeInfo.getParentId(),
                snapshotInfos,
                roundNumber
        );
        if(log.isDebugEnabled()){
            log.debug("--->sending StateMessage: {}", message);
        }
        template.convertAndSend("/topic/stateMessage", message);
        log.trace("StateMessage message sent");
    }
}
