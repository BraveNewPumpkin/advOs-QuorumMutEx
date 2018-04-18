package Node;

import java.util.ArrayList;
import java.util.List;

public class NodeIncrementableRoundSynchronizer<T extends RoundSynchronizable> extends NodeMessageRoundSynchronizer<T> {
    public final List<Integer> roundProgress;

    public NodeIncrementableRoundSynchronizer(int roundSize) {
        super(roundSize);
        System.out.println("SRS Round Size: "+ roundSize);
        roundProgress = new ArrayList<>();
    }

    @Override
    public void enqueueMessage(T message) {
        int messageRoundNumber = message.getRoundNumber();
        incrementProgressForRound(messageRoundNumber);

        super.enqueueMessage(message);
    }

    public void incrementProgressForRound(int roundNumber) {
        if(roundProgress.size() <= roundNumber) {
            for(int i = roundProgress.size(); i <= roundNumber; i++) {
                roundProgress.add(0);
            }
        }
        int selectedRoundProgress = roundProgress.get(roundNumber);
        selectedRoundProgress++;
        System.out.println("inside incrementProgressForRound Roundnumber: "+ roundNumber + "selectedRoundProgress: " +selectedRoundProgress);
        roundProgress.set(roundNumber, selectedRoundProgress);
        ensureQueueForRoundIsInitialized(roundNumber);
    }

    public synchronized void incrementProgressAndRunIfReady(int roundNumber, Runnable work) {
        incrementProgressForRound(roundNumber);
        //only try to run if the round we're progressing is current round
        if(getRoundNumber() == roundNumber) {
            runIfReady(work);
        }
    }

    @Override
    public void runIfReady(Runnable work) {
        int progressSoFarThisRound = roundProgress.get(getRoundNumber());
        System.out.println("inside SRS run if ready, progressSoFarThisRound: "+ progressSoFarThisRound);
        if (progressSoFarThisRound == getRoundSize()) {
            work.run();
        }
    }

    @Override
    public void reset() {
        super.reset();
        roundProgress.clear();
    }
}
