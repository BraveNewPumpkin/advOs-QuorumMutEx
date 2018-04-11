package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;


@Service
@Slf4j
public class SynchGhsService {
    private final SynchGhsController synchGhsController;
    private final ThisNodeInfo thisNodeInfo;

    private int parentUid;
    private boolean isSearched;
    private int phaseNumber;

    @Autowired
    public SynchGhsService(
            @Lazy SynchGhsController synchGhsController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo
    ) {
        this.synchGhsController = synchGhsController;
        this.thisNodeInfo = thisNodeInfo;
        isSearched = false;
        this.phaseNumber =0;
    }

    public void mwoeIntraComponentSearch(int sourceUid, int componentId) {
        parentUid = sourceUid;
        isSearched = true;
        synchGhsController.sendMwoeSearch();
    }

    public void mwoeInterComponentSearch(int sourceUid, int componentId) {
        List<NodeInfo> nodeInfoList = thisNodeInfo.getNeighbors();

        NodeInfo node = null;
        for(NodeInfo tempNode: nodeInfoList)
        {
            if(tempNode.getUid()==sourceUid)
            {
                node = tempNode;
                break;
            }
        }
        log.info("Nodeinfo node is " + node.getUid());
        Edge candidate = thisNodeInfo.getEdges().get(node);
        synchGhsController.sendMwoeCandidate(sourceUid, candidate);
    }

    public void calcLocalMin(List<Edge> candidates) {
        if(isThisNodeLeader()) {
            if(thisNodeInfo.getTreeEdges().size() == 0) {
                //TODO call merge procedure
            } else {
                //TODO send notification to node with MWOE
            }
        } else {
            Collections.sort(candidates);
            Edge localMin = candidates.get(0);
            synchGhsController.sendMwoeCandidate(parentUid, localMin);
        }
    }

    public boolean isThisNodeLeader(){
        return thisNodeInfo.getComponentId() == thisNodeInfo.getUid();
    }

    public boolean isFromComponentNode(int componentId) {
        return thisNodeInfo.getComponentId() == componentId;
    }

    public void markAsSearched() {
        isSearched = true;
    }

    public boolean isSearched() {
        return isSearched;
    }

    public int getPhaseNumber() {
        return phaseNumber;
    }

    public void setPhaseNumber(int phaseNumber) {
        this.phaseNumber = phaseNumber;
    }

    public int getParentUid() {
        return parentUid;
    }

}
