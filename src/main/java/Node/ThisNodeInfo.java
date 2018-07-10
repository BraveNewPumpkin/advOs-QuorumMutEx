package Node;

import java.util.*;

import static java.lang.Math.max;

public final class ThisNodeInfo extends NodeInfo{
    private final List<NodeInfo> quorum;
    private final Set<Integer> allNodeUids;
    private final int totalNumberOfNodes;
    private final int interRequestDelay;
    private final int csExecutionTime;
    private final int scalarClock;


    ThisNodeInfo(
            int uid,
            int totalNumberOfNodes,
            Set<Integer> allNodeUids,
            String hostName,
            int port,
            ) {
        super(uid, hostName, port);
        this.allNodeUids = allNodeUids;
        quorum = new ArrayList<>();
        this.totalNumberOfNodes = totalNumberOfNodes;
    }

    public boolean addNeighbor(NodeInfo neighbor){
        return quorum.add(neighbor);
    }

    public List<NodeInfo> getQuorum() {
        return quorum;
    }

    public int getTotalNumberOfNodes() {
        return totalNumberOfNodes;
    }

    public Set<Integer> getAllNodeUids() {
        return allNodeUids;
    }

}
