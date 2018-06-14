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

    private Object maxNumberSynchronizer; //used for marker messages
    private SnapshotInfo snapshotInfo;
    private TreeInfo treeInfo;
    private Tree<Integer> tree;

    @Autowired
    public SnapshotService(
            @Lazy SnapshotController snapshotController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/maxNumberSynchronizer") Object maxNumberSynchronizer,
            @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo
    ) {
        this.snapshotController = snapshotController;
        this.thisNodeInfo = thisNodeInfo;
        this.maxNumberSynchronizer = maxNumberSynchronizer;
        this.snapshotInfo = snapshotInfo;

        treeInfo = new TreeInfo();
    }

    public void setParent(int parentId) {
        treeInfo.setParentId(parentId);
    }

    public boolean hasParent(){
        return treeInfo.hasParent();
    }

    public void doBuildTreeAckThings(int childUid, Tree<Integer> childTree){
        //add child to list of children and child trees
        treeInfo.addChild(childTree);
        checkAndSendAck();
    }

    public void doBuildTreeNackThings(){
        checkAndSendAck();
    }

    public Tree<Integer> genTree(){
        Tree<Integer> tree = new Tree<Integer>();
        Tree.Node<Integer> rootNode = new Tree.Node<Integer>();
        rootNode.setData(thisNodeInfo.getUid());
        tree.setRoot(rootNode);
        tree.addChildren(treeInfo.getChildren());
        return tree;
    }

    private void checkAndSendAck(){
        treeInfo.incrementBuildTreeResponsesReceived();
        //have we recieved all messages from all children?
        if(hasResponseFromAllNeighbors()) {
            if(thisNodeInfo.getUid() == 0) {
                tree=genTree();
                printTree();
            } else {
                snapshotController.sendBuildTreeAckMessage(treeInfo.getParentId());
            }
        }
    }

    private void printTree(){
        log.info("tree in root: {}", tree);
    }

    private boolean hasResponseFromAllNeighbors() {
        return treeInfo.getBuildTreeResponsesReceived() == thisNodeInfo.getNeighbors().size();
    }
}
