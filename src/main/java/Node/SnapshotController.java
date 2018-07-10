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
    private final SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private final MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer;
    private final Semaphore sendingFifoSynchronizer;
    private final MutableWrapper<Integer> currentMarkRoundNumber;

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
            @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
            MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
            @Qualifier("Node/SnapshotConfig/fifoResponseRoundSynchronizer")
            MessageRoundSynchronizer<FifoRequestId, FifoResponseMessage> fifoResponseRoundSynchronizer,
            @Qualifier("Node/NodeConfigurator/sendingFifoSynchronizer")
            Semaphore sendingFifoSynchronizer,
            @Qualifier("Node/SnapshotConfig/currentMarkRoundNumber")
            MutableWrapper<Integer> currentMarkRoundNumber
    ){
        this.snapshotService = snapshotService;
        this.template = template;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.fifoResponseRoundSynchronizer = fifoResponseRoundSynchronizer;
        this.sendingFifoSynchronizer = sendingFifoSynchronizer;
        this.currentMarkRoundNumber = currentMarkRoundNumber;

        markedSynchronizer = new Object();
        doingStateOrMarkingThings = new Object();
    }

    @MessageMapping("/markMessage")
    public void receiveMarkMessage(MarkMessage message) {

        //spawn in separate thread to allow the message processing thread to return to threadpool
        Runnable doMarkingThings = () -> {
            synchronized (markedSynchronizer) {
                if (log.isDebugEnabled()) {
                    log.debug("<---received MarkMessage {}. Current round {}", message, currentMarkRoundNumber);
                }

                snapshotService.checkAndSendMarkerMessage(message.getRoundId(), message.getSourceUID(), message.getFifoRequestId());
            }
        };
        Thread markingThingsThread = new Thread(doMarkingThings);
        markingThingsThread.start();
    }

    @MessageMapping("/stateMessage")
    public void receiveStateMessage(StateMessage message) {
//        log.debug("Inside receiveStateMessage for round {}",message.getRoundId());
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
        if(thisNodeInfo.getUid() == message.getTarget()) {
            if (log.isDebugEnabled()) {
                log.debug("<---received markResponseMessage {}", message);
            }
            Runnable doReleaseSendingFifoSynchronizer = () -> {
//                log.debug("Releasing sendingFifoSynchronizer for round Id {}", message.getRoundId());
                sendingFifoSynchronizer.release();
            };
            fifoResponseRoundSynchronizer.enqueueAndRunIfReadyInOrder(message, doReleaseSendingFifoSynchronizer);
        }
    }

    public void sendFifoResponse(int targetUid, FifoRequestId fifoRequestId) throws MessagingException {
        FifoResponseMessage message = new FifoResponseMessage(
                thisNodeInfo.getUid(),
                targetUid,
                fifoRequestId
        );

        if(log.isDebugEnabled()){
            log.debug("--->sending fifoResponseMessage: {}", message);
        }
        template.convertAndSend("/topic/markResponseMessage", message);
        log.trace("fifoResponseMessage message sent");
    }

    public void sendMarkMessage(int roundNumber) throws MessagingException {
        try {
            sendingFifoSynchronizer.acquire();
        } catch (java.lang.InterruptedException e) {
            //ignore
        }

        //save state after acquire to prevent map message from being sent after state was saved but before mark was sent
        snapshotService.saveState(roundNumber);

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
