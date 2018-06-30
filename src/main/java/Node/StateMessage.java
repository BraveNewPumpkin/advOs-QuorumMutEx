package Node;

import java.util.Map;

public class StateMessage extends SimpleTargetableMessage implements RoundSynchronizable<Integer>{
    private Map<Integer, SnapshotInfo> snapshotInfo;
    private int snapshotNumber;

    public StateMessage() {
    }

    public StateMessage(int sourceUID, int target, Map<Integer, SnapshotInfo> snapshotInfo, int snapshotNumber) {
        super(sourceUID, target);
        this.snapshotInfo = snapshotInfo;
        this.snapshotNumber = snapshotNumber;
    }

    public Map<Integer, SnapshotInfo> getSnapshotInfos() {
        return snapshotInfo;
    }

    public void setSnapshotInfos(Map<Integer, SnapshotInfo> snapshotInfo) {
        this.snapshotInfo = snapshotInfo;
    }

    public int getSnapshotNumber() {
        return snapshotNumber;
    }

    public void setSnapshotNumber(int snapshotNumber) {
        this.snapshotNumber = snapshotNumber;
    }

    public void setRoundId(Integer roundId){
        snapshotNumber = roundId;
    }

    public Integer getRoundId(){
        return snapshotNumber;
    }

    @Override
    public String toString() {
        return "StateMessage{" +
                "snapshotNumber=" + snapshotNumber +
                "} " + super.toString();
    }
}
