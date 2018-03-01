package Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NodeMessageRoundSynchronizer<T extends RoundSynchronizableMessage> {
    private final List<Queue<T>> roundMessages;

    private int roundNumber;

   public NodeMessageRoundSynchronizer() {
       roundNumber = 0;
       roundMessages = new ArrayList<>(1);
       roundMessages.add(new ConcurrentLinkedQueue<T>());
   }

    public Queue<T> getMessagesThisRound() {
        return roundMessages.get(roundNumber);
    }

    public void enqueueMessage(T message) {
        int messageRoundNumber = message.getRoundNumber();
        int currentRoundIndex = roundMessages.size() - 1;
        if(currentRoundIndex != messageRoundNumber){
            for(int i = currentRoundIndex; i <= messageRoundNumber; i++) {
                roundMessages.add(new ConcurrentLinkedQueue<>());
            }
        }
        roundMessages.get(messageRoundNumber).add(message);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void incrementRoundNumber() {
        roundNumber++;
    }
}
