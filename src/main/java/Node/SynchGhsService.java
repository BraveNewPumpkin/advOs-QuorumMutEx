package Node;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Iterator;
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

    public void calcLocalMin(List<Edge> candidates)  {
        Collections.sort(candidates);
        for(Edge e: candidates)
        {
            System.out.println(e.toString());
        }
        Edge localMin = candidates.get(0);
        log.info(thisNodeInfo.getUid()+ " Local min is:{}",  localMin.toString());
        try{
            Thread.sleep(10 * 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if(isThisNodeLeader()) {
//            int targetUID = (thisNodeInfo.getUid()==localMin.getFirstUid()) ?
//                    localMin.getSecondUid() : localMin.getFirstUid();
//            if(thisNodeInfo.getTreeEdges().size() == 0) {
//                //TODO call merge procedure
//                System.out.println("inside cal min:" + localMin.toString());
//                thisNodeInfo.getTreeEdges().add(localMin);
//                synchGhsController.sendInitiateMerge(targetUID,localMin);
//
//            }
            // changing this logic for first phase, when there is only one node in component
            if(thisNodeInfo.getUid()==localMin.firstUid || thisNodeInfo.getUid()==localMin.secondUid)
            {
                log.debug("Local min found and i am node on mwoe{}", localMin.toString());
                int targetUID = (thisNodeInfo.getUid()==localMin.getFirstUid()) ?
                   localMin.getSecondUid() : localMin.getFirstUid();

                if(!GHSUtil.checkList(thisNodeInfo,localMin)) {
                    log.debug("TreeEdge list does not contain selected MWOE-> {}", localMin.toString());
                    log.debug("thisnodeinfo object id {}", System.identityHashCode(thisNodeInfo));
                    thisNodeInfo.getTreeEdges().add(localMin);
                    synchGhsController.sendInitiateMerge(targetUID, localMin);
                }
                else
                {
                    System.out.println("mwoe already exist, still sending");
                    synchGhsController.sendInitiateMerge(targetUID, localMin);
                    //Todo initiate new leader broadcast
                    log.debug("New leader detection logic triggered");
                    int newLeader = Math.max(localMin.getFirstUid(),localMin.getSecondUid());
                    System.out.println("New Leader is:" + newLeader);

                    thisNodeInfo.setPhaseNumber(thisNodeInfo.getPhaseNumber()+1);
                    List<Edge> treeEdgeListSync = thisNodeInfo.getTreeEdges();
                    synchronized(treeEdgeListSync) {
                        for (Iterator<Edge> itr = treeEdgeListSync.iterator(); itr.hasNext(); ) {
                            Edge edge = itr.next();
                            int sendTo;
                            if (edge.firstUid != thisNodeInfo.getUid())
                                sendTo = edge.firstUid;
                            else
                                sendTo = edge.secondUid;

                            if (sendTo != targetUID) {
                                System.out.println("sending new leader message to " + sendTo);
                                synchGhsController.sendNewLeader(sendTo);
                            }
                        }
                    }

                    //synchGhsController.sendNewLeader(targetUID);
                }
            }
            else {
                //TODO send notification to node with MWOE
                List<Edge> treeEdgeListSync = thisNodeInfo.getTreeEdges();
                synchronized(treeEdgeListSync) {
                    for (Iterator<Edge> itr = treeEdgeListSync.iterator(); itr.hasNext();) {
                        Edge e = itr.next();
                        int localTarget = (thisNodeInfo.getUid() == e.getFirstUid()) ?
                                e.getSecondUid() : e.getFirstUid();
                        System.out.println("LocalTarget:" + localTarget);
                        synchGhsController.sendInitiateMerge(localTarget, localMin);
                    }
                }
            }
        } else {
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