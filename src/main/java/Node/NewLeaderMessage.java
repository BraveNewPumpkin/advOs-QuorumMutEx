package Node;

public class NewLeaderMessage extends SimpleTargetableMessage {

    int newLeaderUID;

    public NewLeaderMessage() {
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

    @Override
    public String toString() {
        return "NewLeaderMessage{" +
                "newLeaderUID=" + newLeaderUID +
                "} " + super.toString();
    }
}
