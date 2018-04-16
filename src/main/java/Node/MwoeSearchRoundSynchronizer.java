package Node;

import java.util.ArrayList;
import java.util.List;

public class MwoeSearchRoundSynchronizer extends NodeMessageRoundSynchronizer<MwoeCandidateMessage> {
    public final List<Integer> roundProgress;

    public MwoeSearchRoundSynchronizer(int roundSize) {
        super(roundSize);
        System.out.println("SRS Round Size: "+ roundSize);
        roundProgress = new ArrayList<>();
    }

    @Override
    public void enqueueMessage(MwoeCandidateMessage message) {
        int messageRoundNumber = message.getRoundNumber();
        int currentRoundIndex = roundProgress.size() - 1;
        System.out.println(" SRSync : Current Rounde Index: "+ currentRoundIndex);
        if(currentRoundIndex != messageRoundNumber){
            for(int i = currentRoundIndex; i <= messageRoundNumber; i++) {
                roundProgress.add(0);
            }
        }
        //2 steps because Integer is immutable
        int messageRoundProgress = roundProgress.get(messageRoundNumber);
        messageRoundProgress++;
        roundProgress.set(messageRoundNumber, messageRoundProgress);

        super.enqueueMessage(message);
    }

    public void incrementProgressForRound(int roundNumber) {
        int selectedRoundProgress = roundProgress.get(roundNumber);
        selectedRoundProgress++;
        roundProgress.set(roundNumber, selectedRoundProgress);
    }

    public void incrementProgressAndRunIfReady(int roundNumber, Runnable work) {
        incrementProgressForRound(roundNumber);
        runIfReady(work);
    }

    @Override
    public void runIfReady(Runnable work) {
        System.out.println("SRS run if ready");
        int progressSoFarThisRound = roundProgress.get(getRoundNumber());

        if (progressSoFarThisRound == getRoundSize()) {
            work.run();
        }
    }
}
