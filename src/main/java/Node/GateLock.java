package Node;

/**
 * this class is a synchronization mechanism akin to a lock. It is specialized in that it need only be unlocked once
 * for any number of waiting processes to acquire it
 */
public class GateLock {
    private boolean isOpen;
    public GateLock() {
        isOpen = false;
    }

    public synchronized void open(){
        isOpen = true;
        notifyAll();
    }

    public synchronized void close(){
        isOpen = false;
    }

    public synchronized void enter(){
        while(!isOpen) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }
}
