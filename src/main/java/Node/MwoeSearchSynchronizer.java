package Node;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MwoeSearchSynchronizer<T extends RoundSynchronizable> extends NodeIncrementableRoundSynchronizer<T> {
    private final Queue<T> validMessagesThisPhase;
    private final int numRoundsInPhase;

    public MwoeSearchSynchronizer(int roundSize, int numRoundsInPhase) {
        super(roundSize);
        this.numRoundsInPhase = numRoundsInPhase;
        this.validMessagesThisPhase = new ConcurrentLinkedQueue<>();
    }

    public synchronized void incrementProgressAndRunIfReady(int roundNumber, Runnable endOfRoundWork, Runnable endOfPhaseWork) {
        super.incrementProgressAndRunIfReady(roundNumber, endOfRoundWork);
        if (getRoundNumber() == numRoundsInPhase) {
            endOfPhaseWork.run();
        }
    }

    public void addValidMessage(T message) {
        validMessagesThisPhase.add(message);
    }

    public Queue<T> getValidMessagesThisPhase() {
        return validMessagesThisPhase;
    }

    @Override
    public void reset() {
        super.reset();
        validMessagesThisPhase.clear();
    }
}
