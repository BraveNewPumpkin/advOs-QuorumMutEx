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
    private TreeInfo treeInfo;
    private Tree<Integer> tree;
    private NodeMessageRoundSynchronizer<MarkMessage> snapshotMarkerSynchronizer;
    private NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer;

    private List<Boolean> isMarkedList;

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
        NodeMessageRoundSynchronizer<StateMessage> snapshotStateSynchronizer
    ) {
        this.snapshotController = snapshotController;
        this.thisNodeInfo = thisNodeInfo;
        this.mapInfo = mapInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.tree = tree;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;
        this.snapshotMarkerSynchronizer = snapshotMarkerSynchronizer;

        isMarkedList = new ArrayList<>();
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
        if(!isMarked(messageRoundNumber)){
            setIsMarked(messageRoundNumber,true);
            snapshotController.sendMarkMessage(messageRoundNumber);
        }
    }

    public void doMarkingThings() {
        snapshotInfo.setVectorClock(thisNodeInfo.getVectorClock());
        snapshotInfo.setActive(mapInfo.isActive());

        if(thisNodeInfo.getUid() != 0) {
            //if we are leaf, send state to parent
            if (isLeaf()) {
                Map<Integer, SnapshotInfo> snapshotInfos = new HashMap<>();
                snapshotInfos.put(thisNodeInfo.getUid(), snapshotInfo);
                snapshotController.sendStateMessage(snapshotInfos);
            }
            snapshotMarkerSynchronizer.incrementRoundNumber();
        }
    }

    public synchronized void doStateThings(Map<Integer, SnapshotInfo> snapshotInfos, int snapshotNumber){
        if(thisNodeInfo.getUid() == 0) {
            printStates(snapshotInfos, snapshotNumber);
        } else {
            snapshotController.sendStateMessage(snapshotInfos);
        }
        snapshotStateSynchronizer.incrementRoundNumber();
    }

    private boolean isLeaf() {
        log.warn("tree.getRoot().getChildren(): {}", tree.getRoot().getChildren());
        log.warn("tree.getRoot().getChildren().isEmpty(): {}", tree.getRoot().getChildren().isEmpty());
        log.warn("tree.getRoot().getChildren().size(): {}", tree.getRoot().getChildren().size());
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
