package Node;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private Node<T> root;

    //dummy constructor for unmarshelling
    public Tree() {}

    public Tree(T rootData) {
        root = new Node<T>();
        root.data = rootData;
        root.children = new ArrayList<Node<T>>();
    }

    public void addChildren(List<Tree<T>> childTrees) {
        childTrees.forEach(childTree -> {
            root.children.add(childTree.root);
        });
    }

    public static class Node<T> {
        private T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node() {
        }

        public void setData(T data) {
            this.data = data;
        }

        public void setParent(Node<T> parent) {
            this.parent = parent;
        }

        public void setChildren(List<Node<T>> children) {
            this.children = children;
        }

        public T getData() {
            return data;
        }

        public Node<T> getParent() {
            return parent;
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
