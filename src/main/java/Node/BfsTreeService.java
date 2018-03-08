package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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

     * receiving buildready before first child acknowledge
     * recieveing acknowledge and build from one child before acknowledge from other children
     *
 */

@Service
@Slf4j
public class BfsTreeService {
    private final BfsTreeController bfsTreeController;
    private final ThisNodeInfo thisNodeInfo;
    private final Semaphore electingNewLeader;

    private final Tree<Integer> tree;
    private final List<Integer> children;
    private final List<Tree<Integer>> childTrees;

    private boolean isRootNode;
    private boolean isMarked;
    private boolean isReadyToBuild;
    private boolean isParentAcknowledged;
    private int parentUID;

    @Autowired
    public BfsTreeService(
        @Lazy BfsTreeController bfsTreeController,
        ThisNodeInfo thisNodeInfo,
        @Qualifier("Node/LeaderElectionConfig/electingNewLeader")
        Semaphore electingNewLeader
    ) {
        this.bfsTreeController = bfsTreeController;
        this.thisNodeInfo = thisNodeInfo;
        this.electingNewLeader = electingNewLeader;

        isMarked = false;
        isRootNode = false;
        isReadyToBuild = false;
        isParentAcknowledged = false;
        children = new ArrayList<>();
        tree = new Tree<>(thisNodeInfo.getUid());
        childTrees = new ArrayList<>();
    }

    public void search(int receivedUid, int sourceDistanceFromRoot) {
        //wait until we know our depth before accepting search messages
        if(!isRootNode) {
            try {
                log.trace("waiting for leader election to complete before accepting bfs search");
                electingNewLeader.acquire();
                log.trace("done waiting for leader election to complete");
            } catch (InterruptedException e) {
                log.warn("interrupted while waiting on leader to be elected");
            }
        }
        //check that source distance is root distance
        if(!isMarked && getThisDistanceFromRoot() == sourceDistanceFromRoot){
            isMarked = true;
            this.parentUID = receivedUid;
            bfsTreeController.sendBfsTreeSearch();
            //TODO add mutex to protect this variable
            if(!isParentAcknowledged) {
                isParentAcknowledged = true;
                bfsTreeController.sendBfsTreeAcknowledge();
            }
            if(log.isTraceEnabled()){
                log.trace("uid: {} has parent: {} and distance from root: {}", thisNodeInfo.getUid(), parentUID, getThisDistanceFromRoot());
            }
        }
    }

    public void acknowledge(int sourceUid, int targetUid) {
        if(targetUid == thisNodeInfo.getUid()) {
            children.add(sourceUid);
            if(isRootNode){
                if(children.size() == thisNodeInfo.getNeighbors().size()) {
                    //root and have received ack from all children
                    bfsTreeController.sendBfsReadyToBuildMessage();
                }
            } else if(!isParentAcknowledged) {
                isParentAcknowledged = true;
                bfsTreeController.sendBfsTreeAcknowledge();
            }
        }
    }

    public void buildReady(){
        if(!isReadyToBuild) {
            isReadyToBuild = true;
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
                this.tree.addChildren(childTrees);
                if(isRootNode) {
                    if(log.isInfoEnabled()){
                        log.info("We're Done! tree: {}", this.tree);
                    }
                } else {
                    //send to parent
                    bfsTreeController.sendBfsBuildMessage();
                }
            }
        }
    }

    public Tree<Integer> getTree() {
        return tree;
    }

    public void setRootNode(boolean isRootNode) {
        this.isRootNode = isRootNode;
    }

    public int getThisDistanceFromRoot() {
        return thisNodeInfo.getDistanceToRoot();
    }

    public int getDistanceToNeighborFromRoot() {
        return thisNodeInfo.getDistanceToRoot() + 1;
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
