package Node;

public class Edge implements Comparable<Edge> {
    int firstUid, secondUid;
    int weight;

    public Edge() {
    }

    public Edge(int firstUid, int secondUid, int weight) {
        this.firstUid = firstUid;
        this.secondUid = secondUid;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "firstUid=" + firstUid +
                ", secondUid=" + secondUid +
                ", weight=" + weight +
                '}';
    }

    public int compareTo(Edge edge) {
        if(weight > edge.getWeight()) {
            return 1;
        } else if(weight < edge.getWeight()) {
            return -1;
        } else if(firstUid > edge.getFirstUid()) {
            return 1;
        } else if(firstUid < edge.getFirstUid()) {
            return -1;
        } else if(secondUid > edge.getSecondUid()) {
            return 1;
        } else if(secondUid < edge.getSecondUid()) {
            return -1;
        } else {
            return 0;
        }
    }

    public int getFirstUid() {
        return firstUid;
    }

    public void setFirstUid(int firstUid) {
        this.firstUid = firstUid;
    }

    public int getSecondUid() {
        return secondUid;
    }

    public void setSecondUid(int secondUid) {
        this.secondUid = secondUid;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
