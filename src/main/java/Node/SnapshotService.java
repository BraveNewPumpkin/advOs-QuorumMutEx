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
    private SnapshotInfo snapshotInfo;
    private final TreeInfo treeInfo;
    private Tree<Integer> tree;
    private NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer;
    private final GateLock preparedForSnapshotSynchronizer;

    //protection for if we receive the last marker message from neighbors and last state message from children at the same time
    private final Object processingFinalStateOrMarkerSynchronizer;

    //isMarked booleans for each round
    private List<Boolean> isMarkedList;
    //used to save and defer sending state to parent if we have not yet received all marked messages this round
    private List<Boolean> isStateReadyToSend;
    private List<Map<Integer, SnapshotInfo>> childrenStatesMaps;

    @Autowired
    public SnapshotService(
        @Lazy SnapshotController snapshotController,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/MapConfig/mapInfo") MapInfo mapInfo,
        @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo,
        @Qualifier("Node/BuildTreeConfig/treeInfo") TreeInfo treeInfo,
        @Qualifier("Node/BuildTreeConfig/tree") Tree<Integer> tree,
        @Qualifier("Node/SnapshotConfig/snaphshotMarkerSynchronizer")
        NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer,
        @Qualifier("Node/SnapshotConfig/snaphshotStateSynchronizer")
        NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer,
        @Qualifier("Node/SnapshotConfig/preparedForSnapshotSynchronizer")
        GateLock preparedForSnapshotSynchronizer
    ) {
        this.snapshotController = snapshotController;
        this.thisNodeInfo = thisNodeInfo;
        this.mapInfo = mapInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.tree = tree;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;
        this.preparedForSnapshotSynchronizer = preparedForSnapshotSynchronizer;

        processingFinalStateOrMarkerSynchronizer = new Object();
        isMarkedList = new ArrayList<>();
        isStateReadyToSend = new ArrayList<>();
        childrenStatesMaps = new ArrayList<>();
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

    public synchronized void checkAndSendMarkerMessage(int messageRoundNumber){
        preparedForSnapshotSynchronizer.enter();
        if(!isMarked(messageRoundNumber)){
            setIsMarked(messageRoundNumber,true);
            snapshotController.sendMarkMessage(messageRoundNumber);
        }
    }

    public void doMarkingThings() {
        if(thisNodeInfo.getUid() != 0) {
            //if we are leaf, send state to parent
            int markerRoundNumber = snapshotMarkerSynchronizer.getRoundNumber();
            fillHasStatePendingToIndex(markerRoundNumber);
            synchronized (processingFinalStateOrMarkerSynchronizer) {
                if (isLeaf() || isStateReadyToSend.get(markerRoundNumber)) {
                    Map<Integer, SnapshotInfo> snapshotInfos;
                    if (isLeaf()) {
                        snapshotInfos = saveStateAndCombineSnapshotInfoMaps(new ArrayList<>());
                    } else {
                        snapshotInfos = childrenStatesMaps.get(markerRoundNumber);
                        //update our state because it could be stale by the time we receive the last marker message for the round
                        SnapshotInfo thisSnapshotInfo = snapshotInfos.get(thisNodeInfo.getUid());
                        thisSnapshotInfo.setVectorClock(thisNodeInfo.getVectorClock());
                        thisSnapshotInfo.setActive(mapInfo.isActive());
                    }
                    snapshotController.sendStateMessage(snapshotInfos);
                    snapshotStateSynchronizer.incrementRoundNumber();
                }
            }
            snapshotMarkerSynchronizer.incrementRoundNumber();
        }
    }

    public Map<Integer, SnapshotInfo> saveStateAndCombineSnapshotInfoMaps(List<Map<Integer, SnapshotInfo>> snapshotMaps) {
        Map<Integer, SnapshotInfo> snapshotInfos = new HashMap<>();
        snapshotInfo.setVectorClock(thisNodeInfo.getVectorClock());
        snapshotInfo.setActive(mapInfo.isActive());
        snapshotInfos.put(thisNodeInfo.getUid(), snapshotInfo);
        snapshotMaps.forEach((snapshotMap) -> {
            snapshotInfos.putAll(snapshotMap);
        });
        return snapshotInfos;
    }

    public void fillHasStatePendingToIndex(int index) {
        if(index > isStateReadyToSend.size() - 1) {
            int numEntriesToAdd = index - isStateReadyToSend.size() + 1;
            isStateReadyToSend.addAll(Collections.nCopies(numEntriesToAdd, false));
        }
    }

    public synchronized void doStateThings(List<Map<Integer, SnapshotInfo>> snapshotInfoMaps, int snapshotNumber){
        Map<Integer, SnapshotInfo> snapshotInfos = saveStateAndCombineSnapshotInfoMaps(snapshotInfoMaps);
        if(thisNodeInfo.getUid() == 0) {
            printStates(snapshotInfos, snapshotNumber);
        } else {
            int stateRoundNumber = snapshotStateSynchronizer.getRoundNumber();
            fillHasStatePendingToIndex(stateRoundNumber);
            synchronized (processingFinalStateOrMarkerSynchronizer) {
                //if we've received all the marker messages the send to parent, otherwise defer until we do receive all
                if (snapshotMarkerSynchronizer.getNumMessagesThisRound() == snapshotMarkerSynchronizer.getRoundSize()) {
                    snapshotController.sendStateMessage(snapshotInfos);
                } else {
                    childrenStatesMaps.add(snapshotStateSynchronizer.getRoundNumber(), snapshotInfos);
                    isStateReadyToSend.add(snapshotStateSynchronizer.getRoundNumber(), true);
                }
            }
        }
        snapshotStateSynchronizer.incrementRoundNumber();
    }

    private boolean isLeaf() {
        log.trace("tree.getRoot().getChildren(): {}", tree.getRoot().getChildren());
        log.trace("tree.getRoot().getChildren().isEmpty(): {}", tree.getRoot().getChildren().isEmpty());
        log.trace("tree.getRoot().getChildren().size(): {}", tree.getRoot().getChildren().size());
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
    }
}
