package Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TreeInfo {
    private Optional<Integer> parentId;

    private int buildTreeResponsesReceived;
    List<Tree<Integer>> children;

    public TreeInfo() {
        children = new ArrayList<>();
        buildTreeResponsesReceived = 0;
        parentId = Optional.empty();
    }

    public int getParentId() {
        return parentId.get();
    }

    public void setParentId(int parentId) {
        this.parentId = Optional.of(parentId);
    }

    public boolean hasParent(){
        return parentId.isPresent();
    }

    public List<Tree<Integer>> getChildren() {
        return children;
    }

    public void addChild(Tree<Integer> child) {
        children.add(child);
    }

    public int getNumChildren(){
        return children.size();
    }

    public int getBuildTreeResponsesReceived() {
        return buildTreeResponsesReceived;
    }

    public void incrementBuildTreeResponsesReceived() {
        this.buildTreeResponsesReceived++;
    }
}
