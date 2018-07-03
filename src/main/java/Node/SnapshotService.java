package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class SnapshotService {
    private final SnapshotController snapshotController;
    private final ThisNodeInfo thisNodeInfo;
    private final MapInfo mapInfo;
    private final SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private final Tree<Integer> tree;
    private final MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final GateLock preparedForSnapshotSynchronizer;
    private final MutableWrapper<Integer> currentMarkRoundNumber;

    //isMarked booleans for each round
    private final List<Boolean> isMarkedList;
    //used to save and defer sending state to parent if we have not yet received all marked messages this round
    private final List<Boolean> isStateReadyToSend;
    private final Map<Integer, SnapshotInfo> thisNodeSnapshots;
    private final SnapshotReadWriter snapshotReadWriter;
    private boolean isTerminatedLastRound;

    @Autowired
    public SnapshotService(
        @Lazy SnapshotController snapshotController,
        SnapshotReadWriter snapshotReadWriter,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/MapConfig/mapInfo") MapInfo mapInfo,
        @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo,
        @Qualifier("Node/BuildTreeConfig/treeInfo") TreeInfo treeInfo,
        @Qualifier("Node/BuildTreeConfig/tree") Tree<Integer> tree,
        @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
        MessageIntRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
        @Qualifier("Node/SnapshotConfig/preparedForSnapshotSynchronizer")
        GateLock preparedForSnapshotSynchronizer,
        @Qualifier("Node/SnapshotConfig/currentMarkRoundNumber")
        MutableWrapper<Integer> currentMarkRoundNumber
    ) {
        this.snapshotController = snapshotController;
        this.snapshotReadWriter = snapshotReadWriter;
        this.thisNodeInfo = thisNodeInfo;
        this.mapInfo = mapInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.tree = tree;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.preparedForSnapshotSynchronizer = preparedForSnapshotSynchronizer;
        this.currentMarkRoundNumber = currentMarkRoundNumber;

        isMarkedList = new ArrayList<>();
        isStateReadyToSend = new ArrayList<>();
        thisNodeSnapshots = new HashMap<>();
        isTerminatedLastRound = false;
    }

    public synchronized void checkAndSendMarkerMessage(int messageRoundNumber, int sourceId, FifoRequestId fifoRequestId){
        if(thisNodeInfo.getUid() != 0) {
            preparedForSnapshotSynchronizer.enter();
            if (messageRoundNumber >= currentMarkRoundNumber.get()) {
                if(isMarked(messageRoundNumber)) {
                    throw new Error("got a mark message for round " + messageRoundNumber + ", but we were in round " + currentMarkRoundNumber);
                }
                setIsMarked(messageRoundNumber, true);
                snapshotController.sendMarkMessage(messageRoundNumber);
                currentMarkRoundNumber.set(currentMarkRoundNumber.get() + 1);
                if(isLeaf()) {
                    Map<Integer, SnapshotInfo> snapshotInfos = combineSnapshotInfoMaps(new ArrayList<>(), messageRoundNumber);
                    snapshotController.sendStateMessage(snapshotInfos, messageRoundNumber);
                }
            }
        }
        snapshotController.sendFifoResponse(sourceId, fifoRequestId);
    }

    public synchronized void doStateThings(List<Map<Integer, SnapshotInfo>> snapshotInfoMaps, int snapshotNumber){
        Map<Integer, SnapshotInfo> snapshotInfos = combineSnapshotInfoMaps(snapshotInfoMaps, snapshotStateSynchronizer.getRoundId());
        if(thisNodeInfo.getUid() == 0) {
            if(!isTerminatedLastRound){
                printStates(snapshotInfos, snapshotNumber);
            }
            boolean isTerminated = terminationDetection(snapshotInfos);
            log.debug("Termination Detection: {}",isTerminated);
            isTerminatedLastRound = isTerminated;
        } else {
            int stateRoundNumber = snapshotStateSynchronizer.getRoundId();
            SnapshotInfo thisNodeSnapshot = thisNodeSnapshots.get(stateRoundNumber);
            snapshotInfos.put(thisNodeInfo.getUid(), thisNodeSnapshot);
            snapshotController.sendStateMessage(snapshotInfos, stateRoundNumber);
        }
        snapshotStateSynchronizer.incrementRoundNumber();
    }

    public boolean isMarked(int roundNumber) {
        boolean isMarked = false;
        if(isMarkedList.size() > roundNumber) {
            isMarked = isMarkedList.get(roundNumber);
        }
        return isMarked;
    }

    public void setIsMarked(int messageRoundNumber, boolean isMarkedVal){
        int isMarkedCounter= this.isMarkedList.size()-1;
        if( isMarkedCounter < messageRoundNumber){
            for(int i=isMarkedCounter;i<messageRoundNumber;i++){
                isMarkedList.add(false);
            }
        }
         this.isMarkedList.set(messageRoundNumber, isMarkedVal);
    }

    public void saveState(int roundNumber){
        SnapshotInfo copy = new SnapshotInfo(snapshotInfo, thisNodeInfo.getVectorClock());
        thisNodeSnapshots.put(roundNumber, copy);
        if(log.isTraceEnabled()) {
            List<Integer>  vectorClock = thisNodeSnapshots.get(currentMarkRoundNumber.get()).getVectorClock();
            log.trace("In saveState(), round: {}   vectorClock: {}", currentMarkRoundNumber, vectorClock);
        }
    }

    public Map<Integer, SnapshotInfo> combineSnapshotInfoMaps(
            List<Map<Integer, SnapshotInfo>> snapshotMaps,
            int roundNumber
    ) {
        Map<Integer, SnapshotInfo> snapshotInfos = new HashMap<>();

        //just added, including the additional parameter
        SnapshotInfo snap = thisNodeSnapshots.get(roundNumber);
        snapshotInfos.put(thisNodeInfo.getUid(), snap);

        snapshotMaps.forEach((snapshotMap) -> {
            snapshotInfos.putAll(snapshotMap);
        });
        return snapshotInfos;
    }

    public boolean terminationDetection(Map<Integer,SnapshotInfo> snapshotInfos) {
        int totalSentMessages = 0;
        int totalProcessedMessages = 0;
        boolean areStatesActive = false;
        boolean isConsistent = true;

        int n = thisNodeInfo.getTotalNumberOfNodes();
        int[][] snapshotMatrix = new int[n][n];

        Set entrySet = snapshotInfos.entrySet();
        Iterator it = entrySet.iterator();

        while (it.hasNext()) {
            Map.Entry map = (Map.Entry) it.next();
            int key = (int) map.getKey();
            SnapshotInfo snapInfo = (SnapshotInfo) map.getValue();

            totalSentMessages += snapInfo.getSentMessages();
            totalProcessedMessages += snapInfo.getProcessedMessages();

            areStatesActive = areStatesActive || snapInfo.isActive();

            for (int i = 0; i < n; i++) {
                snapshotMatrix[key][i] = snapInfo.getVectorClock().get(i);
            }
        }

        for (int q = 0; q < n; q++) {
            int maxVal = 0;
            for (int p = 0; p < n; p++) {
                int val = snapshotMatrix[p][q];
                if (maxVal < val)
                    maxVal = val;
            }
            if (maxVal != snapshotMatrix[q][q]) {
                isConsistent = false;
                break;
            }
        }

        if (isConsistent) {
            log.debug("The Snapshot is Consistent");
        } else {
            //TODO remove
            log.debug("The Snapshot is Not Consistent");
            //throw new Error("was NOT consistant in round " + snapshotStateSynchronizer.getRoundId());
        }
//        log.debug("Total Sent Messages = {} and Total Received Messages = {}", totalSentMessages,totalProcessedMessages);
//        log.debug("isActive: {}",areStatesActive);

//        if(!areStatesActive && totalSentMessages==(totalProcessedMessages-thisNodeInfo.getTotalNumberOfNodes()))
        if(!areStatesActive && totalSentMessages==(totalProcessedMessages-1))
            return true;
        else
            return false;
    }

    private boolean isLeaf() {
        if(tree.getRoot().getChildren().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void printStates(Map<Integer, SnapshotInfo> snapshotInfos, int snapshotNumber) {
        if(log.isInfoEnabled()) {
            log.info("snapshot {}: {}", snapshotNumber, Arrays.toString(snapshotInfos.entrySet().toArray()));
        }
        snapshotReadWriter.writeSnapshots(snapshotInfos);
    }
}
