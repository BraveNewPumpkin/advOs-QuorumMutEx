package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/*
	1. root: send search message
	2. on receiving any message if targetUID != thisUID, suppress
	3. on receive search message
		a. if not marked, mark
		b. if marked, suppress
		c. choose parent
		d. send search message
			i. with d'+1
		e. send acknowledgement message
			i. with d
			ii. with targetUID = parentUID
	4. on receiving acknowledgement message
		a. if targetUID != thisUID suppress
		b. rebroadcast
            i. with targetUID := parentUID
 */

@Service
@Slf4j
public class BfsTreeService {
    private final BfsTreeController bfsTreeController;
    private final ThisNodeInfo thisNodeInfo;

    private final Tree<Integer> tree;
    private final List<Integer> children;
    private final List<Tree<Integer>> childTrees;

    private boolean isRootNode;
    private int thisDistanceFromRoot;
    private boolean isMarked;
    private boolean isReady;
    private int parentUID;

    @Autowired
    public BfsTreeService(@Lazy BfsTreeController bfsTreeController, ThisNodeInfo thisNodeInfo) {
        this.bfsTreeController = bfsTreeController;
        this.thisNodeInfo = thisNodeInfo;

        isMarked = false;
        isReady = false;
        children = new ArrayList<>();
        tree = new Tree<>(thisNodeInfo.getUid());
        childTrees = new ArrayList<>();
    }

    public void search(int receivedUid, int thisDistanceFromRoot) {
        if(!isMarked){
            isRootNode = false;
            isMarked = true;
            this.parentUID = receivedUid;
            this.thisDistanceFromRoot = thisDistanceFromRoot;
            bfsTreeController.sendBfsTreeSearch();
            bfsTreeController.sendBfsTreeAcknowledge();
            if(log.isTraceEnabled()){
                log.trace("uid: {} has distance from root: {}", thisNodeInfo.getUid(), thisDistanceFromRoot);
            }
        }
    }

    public void acknowledge(int sourceUid, int targetUid) {
        if(targetUid == thisNodeInfo.getUid()) {
            children.add(sourceUid);
            //root and have received ack from all children
            if(isRootNode && children.size() == thisNodeInfo.getNeighbors().size()){
                bfsTreeController.sendBfsReadyToBuildMessage();
            } else {
                bfsTreeController.sendBfsTreeAcknowledge();
            }
        }
    }

    public void buildReady(){
        if(!isReady) {
            isReady = true;
            if(children.isEmpty()) {
                bfsTreeController.sendBfsBuildMessage();
            } else {
                bfsTreeController.sendBfsReadyToBuildMessage();
            }
        }
    }

    public void build(int targetUid, Tree<Integer> tree){
        if(targetUid == thisNodeInfo.getUid()) {
            childTrees.add(tree);
            //we've received a subtree from all children
            if(childTrees.size() == children.size()) {
                //add subtrees as children
                tree.addChildren(childTrees);
                //send to parent
                bfsTreeController.sendBfsBuildMessage();
            }
        }
    }

    public Tree<Integer> getTree() {
        return tree;
    }

    public void setRootNode(boolean rootNode) {
        isRootNode = rootNode;
    }

    public void setThisDistanceFromRoot(int thisDistanceFromRoot) {
        this.thisDistanceFromRoot = thisDistanceFromRoot;
    }

    public int getThisDistanceFromRoot() {
        return thisDistanceFromRoot;
    }

    public int getDistanceToNeighborFromRoot() {
        return thisDistanceFromRoot + 1;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public int getParentUID() {
        return parentUID;
    }
}
