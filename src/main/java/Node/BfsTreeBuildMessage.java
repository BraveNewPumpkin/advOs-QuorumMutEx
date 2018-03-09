package Node;

public class BfsTreeBuildMessage extends NodeMessage{
    private Tree<Integer> tree;
    private int parentUid;

    public BfsTreeBuildMessage() {
    }

    public BfsTreeBuildMessage(int parentUid, Tree<Integer> tree) {
        this.parentUid = parentUid;
        this.tree = tree;
    }

    public BfsTreeBuildMessage(int sourceUID, int parentUid, Tree<Integer> tree) {
        super(sourceUID);
        this.parentUid = parentUid;
        this.tree = tree;
    }

    public Tree<Integer> getTree() {
        return tree;
    }

    public int getParentUid() {
        return parentUid;
    }

    @Override
    public String toString() {
        return "BfsTreeBuildMessage{" +
                "tree=" + tree +
                ", parentUid=" + parentUid +
                "} " + super.toString();
    }
}
