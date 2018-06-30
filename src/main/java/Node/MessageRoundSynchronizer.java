package Node;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class MessageRoundSynchronizer<T, M extends RoundSynchronizable<T>> {
    protected final Map<T, Queue<M>> roundMessages;
    private int roundSize;
    private T roundId;

   public MessageRoundSynchronizer(int roundSize) {
       this.roundSize = roundSize;

       roundMessages = new HashMap<>(1);
   }

   //no bounds safety
    public Queue<M> getMessagesForGivenRound(T givenRoundId) {
        return roundMessages.get(givenRoundId);
    }

    public Queue<M> getMessagesThisRound() {
        return roundMessages.get(roundId);
    }

    public int getNumMessagesForGivenRound(T givenRoundId) {
        return getMessagesForGivenRound(givenRoundId).size();
    }

    public int getNumMessagesThisRound() {
       return getNumMessagesForGivenRound(roundId);
    }

    //NOT threadsafe
    public void enqueueMessage(M message) {
        T messageRoundId = message.getRoundId();
        if(roundMessages.containsKey(messageRoundId)) {
            roundMessages.get(messageRoundId).add(message);
        } else {
            Queue queue = new ConcurrentLinkedQueue<>();
            queue.add(message);
            roundMessages.put(messageRoundId, queue);
        }
    }

    //threadsafe
    public synchronized void enqueueAndRunIfReadyInOrder(M message, Runnable work) {
        enqueueMessage(message);
        //only try running if the message round is current round
        if(roundId.equals(message.getRoundId())) {
            this.runCurrentRoundIfReady(work);
        }
    }

    //threadsafe
    public synchronized void enqueueAndRunIfReadyNotInOrder(M message, Runnable work) {
        enqueueMessage(message);
        runGivenRoundIfReady(work, message.getRoundId());
    }

    //NOT threadsafe
    public void runCurrentRoundIfReady(Runnable work) {
        runGivenRoundIfReady(work, roundId);
    }

    //NOT threadsafe
    public void runGivenRoundIfReady(Runnable work, T givenRoundId) {
        int numberOfMessagesSoFar = getMessagesForGivenRound(givenRoundId).size();
        if(log.isTraceEnabled()) {
            log.trace(" numberOfMessagesSoFarThisRound: {} roundId: {}", numberOfMessagesSoFar, roundId);
        }
        if (numberOfMessagesSoFar == roundSize) {
            work.run();
        }
    }

    public T getRoundId() {
        return roundId;
    }

    public void setRoundId(T roundId) {
        this.roundId = roundId;
    }

    public int getRoundSize() {
        return roundSize;
    }

    public void setRoundSize(int roundSize) {
        this.roundSize = roundSize;
    }

    public void reset() {
       roundMessages.clear();
    }
}
