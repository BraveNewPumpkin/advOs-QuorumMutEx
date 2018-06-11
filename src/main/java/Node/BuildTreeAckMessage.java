package Node;

public class BuildTreeAckMessage extends SimpleTargetableMessage {
    private Tree<Integer> tree;

    public BuildTreeAckMessage() {
    }

    public BuildTreeAckMessage(int sourceUID, int target, Tree<Integer> tree) {
        super(sourceUID, target);
        this.tree = tree;
    }

    public Tree<Integer> getTree() {
        return tree;
    }

    public void setTree(Tree<Integer> tree) {
        this.tree = tree;
    }

    @Override
    public String toString() {
        return "BuildTreeAckMessage{" +
                "tree=" + tree +
                "} " + super.toString();
    }
}
