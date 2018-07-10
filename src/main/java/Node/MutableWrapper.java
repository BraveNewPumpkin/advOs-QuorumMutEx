package Node;

public class MutableWrapper<T> {
    private T wrapped;

    public MutableWrapper() {
    }

    public MutableWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    public void set(T object) {
        this.wrapped = object;
    }

    public T get() {
        return wrapped;
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }
}
