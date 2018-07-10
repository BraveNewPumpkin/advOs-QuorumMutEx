package Node;

import java.util.*;

import static java.lang.Math.max;

public final class ThisNodeInfo extends NodeInfo{
    private final List<NodeInfo> quorum;
    private final int totalNumberOfNodes;
    private final int numberOfRequests;
    private final int scalarClock;


    ThisNodeInfo(
            int uid,
            int totalNumberOfNodes,
            String hostName,
            int port,
            int numberOfRequests
            ) {
        super(uid, hostName, port);
        quorum = new ArrayList<>();
        this.totalNumberOfNodes = totalNumberOfNodes;
        this.numberOfRequests=numberOfRequests;
        scalarClock=0;
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

    public int getNumberOfRequests() {
        return numberOfRequests;
    }
}
