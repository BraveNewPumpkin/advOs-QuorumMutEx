package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final GateLock electingNewLeader;

    private final Tree<Integer> tree;
    /** tracks non-parent neighbors from which I have received an bfs acknowledge**/
    private final Set<Integer> neighborsAcknowledged;
    private final List<Integer> children;
    private final List<Tree<Integer>> childTrees;
    private final GateLock allAcknowledgedBeforeReadyToBuildLock;

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
        GateLock electingNewLeader
    ) {
        this.bfsTreeController = bfsTreeController;
        this.thisNodeInfo = thisNodeInfo;
        this.electingNewLeader = electingNewLeader;

        isMarked = false;
        isRootNode = false;
        isReadyToBuild = false;
        isParentAcknowledged = false;
        neighborsAcknowledged = new HashSet<>(thisNodeInfo.getNeighbors().size() - 1 );
        children = new ArrayList<>();
        tree = new Tree<>(thisNodeInfo.getUid());
        childTrees = new ArrayList<>();
        allAcknowledgedBeforeReadyToBuildLock = new GateLock();
        allAcknowledgedBeforeReadyToBuildLock.close();
    }

    public void search(int receivedUid, int sourceDistanceFromRoot) {
        //wait until we know our depth before accepting search messages
        //NOTE checking isMarked here does not prevent any race conditions, it is convenience only to reduce
        // redundancies in log
        if(!isRootNode && !isMarked) {
            log.trace("waiting for leader election to complete before accepting bfs search");
            electingNewLeader.enter();
            log.trace("done waiting for leader election to complete");
        }
        //check that source distance is root distance
        if(!isMarked && getThisDistanceFromRoot() == sourceDistanceFromRoot){
            isMarked = true;
            this.parentUID = receivedUid;
            bfsTreeController.sendBfsTreeSearch();
            //TODO add mutex to protect this variable
            if(!isParentAcknowledged) {
                isParentAcknowledged = true;
                if (thisNodeInfo.getNeighbors().size() == 1) {
                    log.trace("I have no non-parent neighbors. opening ready to build gate.");
                    allAcknowledgedBeforeReadyToBuildLock.open();
                }
                bfsTreeController.sendBfsTreeAcknowledge();
            }
            if(log.isTraceEnabled()){
                log.trace("uid: {} has parent: {} and distance from root: {}", thisNodeInfo.getUid(), parentUID, getThisDistanceFromRoot());
            }
        }
    }

    public void acknowledge(int sourceUid, int targetUid) {
        synchronized (this) {
            neighborsAcknowledged.add(sourceUid);
            if (neighborsAcknowledged.size() == thisNodeInfo.getNeighbors().size() - 1) {
                log.trace("received acknowledge from all non-parent neighbors. opening ready to build gate.");
                allAcknowledgedBeforeReadyToBuildLock.open();
            }
        }
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
        log.trace("waiting on all non-parent neighbors to acknowledge to enter ready to build gate");
        allAcknowledgedBeforeReadyToBuildLock.enter();
        log.trace("done waiting on all non-parent neighbors to acknowledged. Ready to build.");
        if(!isReadyToBuild) {
            isReadyToBuild = true;
            if (children.isEmpty()) {
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

    public boolean isReadyToBuild() {
        return isReadyToBuild;
    }

    public int getParentUID() {
        return parentUID;
    }
}
