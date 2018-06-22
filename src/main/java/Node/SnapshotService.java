package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SnapshotService {
    private final SnapshotController snapshotController;
    private final ThisNodeInfo thisNodeInfo;
    private SnapshotInfo snapshotInfo;
    private TreeInfo treeInfo;
    private Tree<Integer> tree;
    private NodeMessageRoundSynchronizer<MarkMessage> snapshotSynchronizer;

    private boolean isMarked;

    @Autowired
    public SnapshotService(
        @Lazy SnapshotController snapshotController,
        @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo,
        @Qualifier("Node/BuildTreeConfig/treeInfo") TreeInfo treeInfo,
        @Qualifier("Node/BuildTreeConfig/tree") Tree<Integer> tree,
        @Qualifier("Node/SnapshotConfig/snaphshotSynchronizer")
        NodeMessageRoundSynchronizer<MarkMessage> snapshotSynchronizer
    ) {
        this.snapshotController = snapshotController;
        this.thisNodeInfo = thisNodeInfo;
        this.snapshotInfo = snapshotInfo;
        this.treeInfo = treeInfo;
        this.tree = tree;
        this.snapshotSynchronizer = snapshotSynchronizer;

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
            snapshotController.sendStateMessage();
        }
        snapshotSynchronizer.incrementRoundNumber();
    }

    public synchronized void doStateThings(){
        //TODO implement
        //save state in matrix
        //if we have state from all children this round
            //send all states including own to parent
    }

    private boolean isLeaf() {
        if(tree.getRoot().getChildren().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
