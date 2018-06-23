package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    private boolean isMarked;

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
        this.snapshotStateSynchronizer = snapshotStateSynchronizer;

        isMarked = false;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void doMarkingThings() {
        isMarked = true;
        snapshotController.sendMarkMessage();
        //if we are leaf, send state to parent
        if(isLeaf()){
            Map<Integer, SnapshotInfo> snapshotInfos = new HashMap<>();
            snapshotInfos.put(thisNodeInfo.getUid(), snapshotInfo);
            snapshotController.sendStateMessage(snapshotInfos);
        }
        snapshotInfo.setVectorClock(thisNodeInfo.getVectorClock());
        snapshotInfo.setActive(mapInfo.isActive());
        snapshotMarkerSynchronizer.incrementRoundNumber();
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
        if(tree.getRoot().getChildren().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void printStates(Map<Integer, SnapshotInfo> snapshotInfos, int snapshotNumber) {

    }
}
