package Node;

public class CsRequesterInfo {
    private final int interRequestDelay;
    private final int csExecutionTime;

    public CsRequesterInfo(int interRequestDelay, int csExecutionTime) {
        this.interRequestDelay = interRequestDelay;
        this.csExecutionTime = csExecutionTime;
    }

    public int getInterRequestDelay() {
        return interRequestDelay;
    }

    public int getCsExecutionTime() {
        return csExecutionTime;
    }


}
