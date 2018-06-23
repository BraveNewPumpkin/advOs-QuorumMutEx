package Node;

import java.util.*;

import static java.lang.Math.max;

public final class ThisNodeInfo extends NodeInfo{
    private final List<NodeInfo> neighbors;
    private final int totalNumberOfNodes;
    private final int minPerActive;
    private final int maxPerActive;
    private final int minSendDelay;
    private final int snapshotDelay;
    private final int maxNumber;
    private List<Integer> vectorClock;


    ThisNodeInfo(
            int uid,
            int totalNumberOfNodes,
            String hostName,
            int port,
            int minPerActive,
            int maxPerActive,
            int minSendDelay,
            int snapshotDelay,
            int maxNumber
            ) {
        super(uid, hostName, port);
        neighbors = new ArrayList<>();
        this.totalNumberOfNodes = totalNumberOfNodes;
        this.minPerActive=minPerActive;
        this.maxPerActive=maxPerActive;
        this.minSendDelay=minSendDelay;
        this.snapshotDelay=snapshotDelay;
        this.maxNumber=maxNumber;
        this.vectorClock = new ArrayList<>();
    }

    public boolean addNeighbor(NodeInfo neighbor){
        return neighbors.add(neighbor);
    }

    public List<NodeInfo> getNeighbors() {
        return neighbors;
    }

    public int getTotalNumberOfNodes() {
        return totalNumberOfNodes;
    }

    public int getMinPerActive() {
        return minPerActive;
    }

    public int getMaxPerActive() {
        return maxPerActive;
    }

    public int getMinSendDelay() {
        return minSendDelay;
    }

    public int getSnapshotDelay() {
        return snapshotDelay;
    }

    public int getMaxNumber() {
        return maxNumber;
    }


    public List<Integer>  getVectorClock() {return vectorClock;}

    public void setVectorClock(List<Integer> vectorClock) {this.vectorClock = vectorClock;}

    public void incrementVectorClock () {
        int incrementedValue = vectorClock.get(this.getUid()) + 1;
        vectorClock.set(this.getUid(), incrementedValue);
    }

    public void mergeVectorClock(List<Integer> mapVectorClock) {
        ListIterator vectorIterator = vectorClock.listIterator();
        Iterator mapVectorIterator = mapVectorClock.iterator();
        while (vectorIterator.hasNext() && mapVectorIterator.hasNext())
        {
            int vectorClockValue = (Integer)vectorIterator.next();
            int mapVectorClockValue = (Integer)mapVectorIterator.next();
            int maxValue = max(vectorClockValue, mapVectorClockValue);
            vectorIterator.set(maxValue);
        }
        incrementVectorClock();
    }

}
