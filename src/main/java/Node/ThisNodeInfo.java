package Node;

import java.util.ArrayList;
import java.util.List;

public final class ThisNodeInfo extends NodeInfo{
    List<NodeInfo> neighbors;
    private int distance;

    ThisNodeInfo(int uid, String hostName, int port) {
        super(uid, hostName, port);
        neighbors = new ArrayList<NodeInfo>();
    }

    public boolean addNeighbor(NodeInfo neighbor){
        return neighbors.add(neighbor);
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }
}
