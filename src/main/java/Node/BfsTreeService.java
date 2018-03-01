package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

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

    private int thisDistanceFromRoot;

    private boolean isMarked;
    private int parentUID;

    @Autowired
    public BfsTreeService(@Lazy BfsTreeController bfsTreeController, ThisNodeInfo thisNodeInfo) {
        this.bfsTreeController = bfsTreeController;
        this.thisNodeInfo = thisNodeInfo;

        isMarked = false;
    }

    public void search(int parentUID, int thisDistanceFromRoot) {
        if(!isMarked){
            //TODO set parent
            isMarked = true;
            this.parentUID = parentUID;
            this.thisDistanceFromRoot = thisDistanceFromRoot;
            bfsTreeController.sendBfsTreeSearch();
        }
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
