package Node;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class NodeMessageRoundSynchronizer<T extends RoundSynchronizable> {
    private final List<Queue<T>> roundMessages;

    private int roundSize;

    private int roundNumber;

   public NodeMessageRoundSynchronizer(int roundSize) {
       this.roundSize = roundSize;

       roundNumber = 0;
       roundMessages = new ArrayList<>(1);
       roundMessages.add(new ConcurrentLinkedQueue<>());
   }

   //no bounds safety
    public Queue<T> getMessagesForGivenRound(int givenRoundNumber) {
        return roundMessages.get(givenRoundNumber);
    }

    public Queue<T> getMessagesThisRound() {
        return roundMessages.get(roundNumber);
    }

    public int getNumMessagesForGivenRound(int givenRoundNumber) {
        int numMessagesForGivenRound = 0;
        if(roundMessages.size() > givenRoundNumber) {
            numMessagesForGivenRound = roundMessages.get(givenRoundNumber).size();
        }
        return numMessagesForGivenRound;
    }

    public int getNumMessagesThisRound() {
       return getNumMessagesForGivenRound(roundNumber);
    }

    //NOT threadsafe
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

    //threadsafe
    public synchronized void enqueueAndRunIfReadyInOrder(T message, Runnable work) {
        enqueueMessage(message);
        //only try running if the message round is current round
        if(roundNumber == message.getRoundNumber()) {
            this.runCurrentRoundIfReady(work);
        }
    }

    //threadsafe
    public synchronized void enqueueAndRunIfReadyNotInOrder(T message, Runnable work) {
        enqueueMessage(message);
        this.runGivenRoundIfReady(work, message.getRoundNumber());
    }

    //NOT threadsafe
    public void runCurrentRoundIfReady(Runnable work) {
        runGivenRoundIfReady(work, roundNumber);
    }

    //NOT threadsafe
    public void runGivenRoundIfReady(Runnable work, int givenRoundNumber) {
        int numberOfMessagesSoFar = getMessagesForGivenRound(givenRoundNumber).size();
        if(log.isTraceEnabled()) {
            log.trace(" numberOfMessagesSoFarThisRound: {} roundNumber: {}", numberOfMessagesSoFar, roundNumber);
        }
        if (numberOfMessagesSoFar == roundSize) {
            work.run();
        }
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public void incrementRoundNumber() {
        if(log.isTraceEnabled()) {
            log.trace("moving from round {} to round {}", roundNumber, roundNumber + 1);
        }
        roundNumber++;
        ensureQueueForRoundIsInitialized(roundNumber);
    }

    public int getRoundSize() {
        return roundSize;
    }

    public void setRoundSize(int roundSize) {
        this.roundSize = roundSize;
    }

    public void ensureQueueForRoundIsInitialized(int roundNumber) {
       if(roundMessages.size() < roundNumber) {
           for(int i = roundMessages.size(); i <= roundNumber; i++) {
               roundMessages.add(new ConcurrentLinkedQueue<>());
           }
       }
    }


    public void reset() {
       roundMessages.clear();
       roundNumber = 0;
    }
}
