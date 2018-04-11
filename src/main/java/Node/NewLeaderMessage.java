package Node;

public class NewLeaderMessage extends SimpleTargetableMessage {

    int newLeaderUID;

    public NewLeaderMessage() {
    }

    public NewLeaderMessage(int sourceUID, int phaseNumber, int target) {
        super(sourceUID, phaseNumber, target);
    }

    public NewLeaderMessage(int sourceId, int phaseNumber, int target, int newLeaderUID){
        super(sourceId, phaseNumber, target);
        this.newLeaderUID = newLeaderUID;
    }

    public int getNewLeaderUID() {
        return newLeaderUID;
    }

    public void setNewLeaderUID(int newLeaderUID) {
        this.newLeaderUID = newLeaderUID;
    }
}
