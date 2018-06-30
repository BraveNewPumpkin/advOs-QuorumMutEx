package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.Semaphore;

@Controller
@Slf4j
public class SnapshotController {
    private final SnapshotService snapshotService;
    private final SimpMessagingTemplate template;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private final MessageIntRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private final MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer;
    private final Semaphore sendingFifoSynchronizer;

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
            MessageIntRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer,
            @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
            MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
            @Qualifier("Node/SnapshotConfig/fifoResponseRoundSynchronizer")
            MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer,
            @Qualifier("Node/NodeConfigurator/sendingFifoSynchronizer")
            Semaphore sendingFifoSynchronizer
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.fifoResponseRoundSynchronizer = fifoResponseRoundSynchronizer;
        this.sendingFifoSynchronizer = sendingFifoSynchronizer;

        markedSynchronizer = new Object();
        doingStateOrMarkingThings = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {
        Runnable doCallMarkingThingsForMessage = () -> {
            snapshotService.doMarkingThings(message.getRoundId());
        };

        //spawn in separate thread to allow the message processing thread to return to threadpool
        Runnable doMarkingThings = () -> {
            synchronized (doingStateOrMarkingThings) {
                if (log.isDebugEnabled()) {
                    log.debug("<---received MarkMessage {}. {} of {} this round", message, snapshotMarkerSynchronizer.getNumMessagesForGivenRound(message.getRoundId()) + 1, snapshotMarkerSynchronizer.getRoundSize());
                }

                snapshotService.checkAndSendMarkerMessage(message.getRoundId(), message.getSourceUID(), message.getFifoRequestId());
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
                        log.debug("<---received StateMessage {}. {} of {} for round {}. current round {}.", message, snapshotStateSynchronizer.getNumMessagesForGivenRound(message.getSnapshotNumber()) + 1, snapshotStateSynchronizer.getRoundSize(), message.getSnapshotNumber(), snapshotStateSynchronizer.getRoundId());
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
                        Queue<StateMessage> messages = snapshotStateSynchronizer.getMessagesForGivenRound(snapshotStateSynchronizer.getRoundId());
                        messages.forEach((StateMessage stateMessage) -> {
                            snapshotInfoMaps.add(stateMessage.getSnapshotInfos());
                        });
                        snapshotService.doStateThings(snapshotInfoMaps, snapshotStateSynchronizer.getRoundId());
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

    @MessageMapping("/markResponseMessage")
    public void receiveFifoResponseMessage(FifoResponseMessage message) {
        if(thisNodeInfo.getUid() != message.getTarget()) {
            if (log.isDebugEnabled()) {
                log.debug("<---received markResponseMessage {}", message);
            }
            Runnable doReleaseSendingFifoSynchronizer = () -> {
                sendingFifoSynchronizer.release();
            };
            fifoResponseRoundSynchronizer.enqueueAndRunIfReadyInOrder(message, doReleaseSendingFifoSynchronizer);
        }
    }

    public void sendFifoResponse(int targetUid, FifoRequestId fifoRequestId) throws MessagingException {
        thisNodeInfo.incrementVectorClock();

        FifoResponseMessage message = new FifoResponseMessage(
                thisNodeInfo.getUid(),
                targetUid,
                fifoRequestId
        );

        if(log.isDebugEnabled()){
            log.debug("--->sending fifoResponseMessage: {}", message);
        }
        template.convertAndSend("/topic/fifoResponseMessage", message);
        log.trace("fifoResponseMessage message sent");
    }

    public void sendMarkMessage(int roundNumber) throws MessagingException {
        try {
            sendingFifoSynchronizer.acquire();
        } catch (java.lang.InterruptedException e) {
            //ignore
        }
        FifoRequestId currentFifoRequestId = new FifoRequestId(thisNodeInfo.getUid()+"mark" + roundNumber);
        fifoResponseRoundSynchronizer.setRoundId(currentFifoRequestId);
        MarkMessage message = new MarkMessage(
                thisNodeInfo.getUid(),
                roundNumber,
                currentFifoRequestId
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
