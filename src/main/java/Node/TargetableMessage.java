package Node;

public interface TargetableMessage<T> {
    T getTarget();
    void setTarget(T target);
}

