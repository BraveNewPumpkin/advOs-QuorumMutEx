package Node;

import java.util.ArrayList;
import java.util.List;

public final class ThisNodeInfo extends NodeInfo{
    List<NodeInfo> neighbors;
    private int distanceToRoot;

    ThisNodeInfo(int uid, String hostName, int port) {
        super(uid, hostName, port);
        neighbors = new ArrayList<NodeInfo>();
    }

    public boolean addNeighbor(NodeInfo neighbor){
        return neighbors.add(neighbor);
    }

    public int getDistanceToRoot() {
        return distanceToRoot;
    }

    public void setDistanceToRoot(int distanceToRoot) {
        this.distanceToRoot = distanceToRoot;
    }

    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }
}
