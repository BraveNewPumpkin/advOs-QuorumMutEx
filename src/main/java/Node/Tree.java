package Node;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private Node<T> root;

    public Tree() {}

    public Tree(T rootData) {
        root = new Node<>();
        root.data = rootData;
        root.children = new ArrayList<>();
    }

    public void addChildren(List<Tree<T>> childTrees) {
        childTrees.forEach(childTree -> {
            root.children.add(childTree.root);
        });
    }

    @Override
    public String toString() {
        return "Tree{" +
                "root=" + root +
                '}';
    }

    public static class Node<T> {
        private T data;
        private List<Node<T>> children;

        public Node() {
        }

        @Override
        public String toString() {
            return "Node{" +
                    "data=" + data +
                    ", children=" + children +
                    '}';
        }

        public void setData(T data) {
            this.data = data;
        }

        public void setChildren(List<Node<T>> children) {
            this.children = children;
        }

        public T getData() {
            return data;
        }

        public List<Node<T>> getChildren() {
            return children;
        }
    }

    public void setRoot(Node<T> root) {
        this.root = root;
    }

    //make it serializable
    public Node<T> getRoot() {
        return root;
    }
}
