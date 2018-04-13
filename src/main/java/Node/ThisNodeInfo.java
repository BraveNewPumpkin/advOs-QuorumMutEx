package Node;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ThisNodeInfo extends NodeInfo{
    private final List<NodeInfo> neighbors;
    private final Map<NodeInfo, Edge> edges;
    private final int totalNumberOfNodes;
    private int componentId;
    private List<Edge> treeEdges;


    ThisNodeInfo(int uid, int totalNumberOfNodes, String hostName, int port) {
        super(uid, hostName, port);
        this.totalNumberOfNodes = totalNumberOfNodes;
        neighbors = new ArrayList<>();
        edges = new HashMap<>();
        treeEdges =  Collections.synchronizedList(new ArrayList<>());
        //initially we are the only node in our component so we own it
        this.componentId = uid;
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

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public synchronized List<Edge> getTreeEdges() {
        return treeEdges;
    }

    public void addTreeEdges(Edge treeEdge) {
        treeEdges.add(treeEdge);
    }


}
