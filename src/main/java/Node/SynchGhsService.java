package Node;

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
    private final NodeIncrementableRoundSynchronizer nodeIncrementableRoundSynchronizer;
    private final MwoeSearchSynchronizer<MwoeSearchMessage> mwoeSearchSynchronizer;

    private int parentUid;
    private boolean isSearched;
    private int phaseNumber;

    @Autowired
    public SynchGhsService(
            @Lazy SynchGhsController synchGhsController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/LeaderElectionConfig/mwoeSearchResponseRoundSynchronizer")
            NodeIncrementableRoundSynchronizer nodeIncrementableRoundSynchronizer,
            @Qualifier("Node/LeaderElectionConfig/mwoeSearchSynchronizer")
            MwoeSearchSynchronizer<MwoeSearchMessage> mwoeSearchSynchronizer
    ) {
        this.synchGhsController = synchGhsController;
        this.thisNodeInfo = thisNodeInfo;
        this.nodeIncrementableRoundSynchronizer = nodeIncrementableRoundSynchronizer;
        this.mwoeSearchSynchronizer = mwoeSearchSynchronizer;

        isSearched = false;
        this.phaseNumber =0;

    }

    public void processSearches(List<MwoeSearchMessage> searchMessages) {

    }

    public void mwoeIntraComponentSearch(int sourceUid) {
        parentUid = sourceUid;
        isSearched = true;
        synchGhsController.sendMwoeSearch(false);
    }

    public void mwoeInterComponentSearch(int sourceUid) {
        List<NodeInfo> nodeInfoList = thisNodeInfo.getNeighbors();

        //find node that sent me this
        NodeInfo node = null;
        for(NodeInfo tempNode: nodeInfoList)
        {
            if(tempNode.getUid() == sourceUid)
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
        if (candidates.size() == 0) {
            callTermination();
            return ;
        }
        Collections.sort(candidates);
        for (Edge e : candidates)
            System.out.println(e.toString());

        Edge localMin = candidates.get(0);
        log.info(thisNodeInfo.getUid() + " Local min is:{}", localMin.toString());
        processMinEdge(localMin);
    }

    public void processMinEdge(Edge localMin){
         if(isThisNodeLeader()) {
            int targetUID =  checkThisEdgeBelongsToMe(thisNodeInfo, localMin);
            if(targetUID!=-1){
                //This edge belongs to me, check if it is already in my tree edge list , if not, add it adn send merge request to target node
                if(addIfTreeEdgeDoesntExist(thisNodeInfo, localMin))
                    synchGhsController.sendInitiateMerge(targetUID, localMin);
                else
                {
                    log.debug("New leader detection logic triggered");
                    triggerNewLeaderElectionAndSend(thisNodeInfo, localMin);
                }
            }
            else {
                //If lcoalMin edge doesn't belongs to me, relay it to all my treeEdges
                relayLocalMinEdge(thisNodeInfo,localMin,-1);
            }
         }
         else {
             //If I am not the leader, convercast localMin to the parent node
            synchGhsController.sendMwoeCandidate(parentUid, localMin);
         }
    }

    public int checkThisEdgeBelongsToMe(ThisNodeInfo node,Edge localMin)
    {
        int targetUID = -1;
        if(thisNodeInfo.getUid()==localMin.firstUid || thisNodeInfo.getUid()==localMin.secondUid)
        {
            log.debug("Local min found and i am node on mwoe{}", localMin.toString());
            targetUID = (thisNodeInfo.getUid()==localMin.getFirstUid()) ?
                    localMin.getSecondUid() : localMin.getFirstUid();
        }
        return targetUID;
    }

    public boolean addIfTreeEdgeDoesntExist(ThisNodeInfo node, Edge localMin)
    {
        if(!GHSUtil.checkList(thisNodeInfo,localMin)) {
            log.debug("TreeEdge list does not contain selected MWOE-> {}", localMin.toString());
            thisNodeInfo.getTreeEdges().add(localMin);
            GHSUtil.printTreeEdgeList(thisNodeInfo.getTreeEdges());
            return true;
        }
        return false;
    }

    public void triggerNewLeaderElectionAndSend(ThisNodeInfo node, Edge localMin){

        log.debug("New leader detection logic triggered");
        int newLeader = Math.max(localMin.getFirstUid(),localMin.getSecondUid());
        System.out.println("New Leader is:" + newLeader);
        moveToNextPhase(newLeader);
        List<Edge> treeEdgeListSync = thisNodeInfo.getTreeEdges();
        synchronized(treeEdgeListSync) {
            for (Iterator<Edge> itr = treeEdgeListSync.iterator(); itr.hasNext(); ) {
                Edge edge = itr.next();
                int sendTo = edge.firstUid != thisNodeInfo.getUid() ?  edge.firstUid : edge.secondUid;
                System.out.println("sending new leader message to " + sendTo);
                synchGhsController.sendNewLeader(sendTo);
            }
        }
        if(node.getUid()==newLeader) {
            System.out.println("Intiating MWOE Search for next round in service");
            synchGhsController.sendMwoeSearch(false);
        }
    }

    public void relayLocalMinEdge(ThisNodeInfo node, Edge localMin, int dontSendToThisUID){
        List<Edge> treeEdgeListSync = node.getTreeEdges();
        synchronized(treeEdgeListSync) {
            for (Iterator<Edge> itr = treeEdgeListSync.iterator(); itr.hasNext();) {
                Edge e = itr.next();
                int localTarget = (node.getUid() == e.getFirstUid()) ? e.getSecondUid() : e.getFirstUid();
                System.out.println("LocalTarget:" + localTarget);
                if(localTarget!=dontSendToThisUID)
                    synchGhsController.sendInitiateMerge(localTarget, localMin);
            }
        }
    }
    public void relayNewLeaderMessage(ThisNodeInfo node, int newLeader, int dontSendToThisUID){
        List<Edge> treeEdgeListSync = node.getTreeEdges();
        synchronized (treeEdgeListSync) {
            for (Iterator<Edge> itr = treeEdgeListSync.iterator(); itr.hasNext(); ) {
                Edge edge = itr.next();
                int targetUID = edge.getFirstUid() != node.getUid() ? edge.getFirstUid() : edge.getSecondUid();
                if (targetUID != dontSendToThisUID)
                    synchGhsController.sendNewLeader(targetUID);
            }
        }
    }

    public void checkIfTargetNodeIsInMyNeighborList(ThisNodeInfo node, int otherComponentNode, Edge selectedMwoeEdge){
        List<NodeInfo> neighbors= thisNodeInfo.getNeighbors();
        for(NodeInfo n: neighbors) {
            if(n.getUid() == otherComponentNode) {
                System.out.println("MWOE Edge added because of Parent message");
                thisNodeInfo.getTreeEdges().add(selectedMwoeEdge);
                GHSUtil.printTreeEdgeList(thisNodeInfo.getTreeEdges());
            }
        }
    }
    public void moveToNextPhase(int newLeader) {
        thisNodeInfo.setComponentId(newLeader);
        System.out.println("isSearched unmarked");
        markAsUnSearched();
        setPhaseNumber(getPhaseNumber()+1);
        mwoeSearchSynchronizer.incrementRoundNumber();
    }

    public void callTermination(){
        System.out.println("Terminating...");
        System.out.println("Leader of the final component is: "+ thisNodeInfo.getComponentId());
        GHSUtil.printTreeEdgeList(thisNodeInfo.getTreeEdges());
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

    public void markAsUnSearched() {
        isSearched = false;
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