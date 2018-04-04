package Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ThisNodeInfo extends NodeInfo{
    private final List<NodeInfo> neighbors;
    private final Map<NodeInfo, Edge> edges;
    private final int totalNumberOfNodes;

    ThisNodeInfo(int uid, int totalNumberOfNodes, String hostName, int port) {
        super(uid, hostName, port);
        this.totalNumberOfNodes = totalNumberOfNodes;
        neighbors = new ArrayList<>();
        edges = new HashMap<>();
    }

    public boolean addNeighbor(NodeInfo neighbor){
        return neighbors.add(neighbor);
    }

    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }

    public Edge addEdge(Edge edge, NodeInfo connectedNode){
        return edges.put(connectedNode, edge);
    }

    public Map<NodeInfo, Edge> getEdges() {
        return edges;
    }

    public int getTotalNumberOfNodes() {
        return totalNumberOfNodes;
    }

}
