package Node;

public class MapInfo {
    private int messagesSent;
    private boolean isActive;

    public MapInfo(){
        messagesSent = 0;
        isActive = false;
    }

    public void incrementMessagesSent() {
        setMessagesSent(getMessagesSent() + 1);
    }

    public int getMessagesSent() {
        return messagesSent;
    }

    public void setMessagesSent(int messagesSent) {
        this.messagesSent = messagesSent;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
