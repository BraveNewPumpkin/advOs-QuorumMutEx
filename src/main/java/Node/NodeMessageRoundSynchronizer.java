package Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NodeMessageRoundSynchronizer<T extends RoundSynchronizable> {
    private final List<Queue<T>> roundMessages;

    private final int roundSize;

    private int roundNumber;

   public NodeMessageRoundSynchronizer(int roundSize) {
       this.roundSize = roundSize;

       roundNumber = 0;
       roundMessages = new ArrayList<>(1);
       roundMessages.add(new ConcurrentLinkedQueue<>());
   }

    public Queue<T> getMessagesThisRound() {
        return roundMessages.get(roundNumber);
    }

    //NOT threadsafe
    public void enqueueMessage(T message) {
        int messageRoundNumber = message.getRoundNumber();
        int currentRoundIndex = roundMessages.size() - 1;
        System.out.println(" NMSync : Current Rounde Index: "+ currentRoundIndex + "Msg round num " + messageRoundNumber);
        if(currentRoundIndex != messageRoundNumber){
            for(int i = currentRoundIndex; i <= messageRoundNumber; i++) {
                roundMessages.add(new ConcurrentLinkedQueue<>());
            }
        }
        roundMessages.get(messageRoundNumber).add(message);
        System.out.println("Message size: " + roundMessages.get(messageRoundNumber).size());
    }

    //threadsafe
    public synchronized void enqueueAndRunIfReady(T message, Runnable work) {
        System.out.println("Message to enqueue: "+message);
        enqueueMessage(message);
        System.out.println("run if ready starts");
        //only try running if the message round is current round
        if(roundNumber == message.getRoundNumber()) {
            this.runIfReady(work);
        }
        System.out.println("run if ready stops");
    }

    //NOT threadsafe
    public void runIfReady(Runnable work) {
        System.out.println("round number L48" + roundNumber);
        int numberOfMessagesSoFarThisRound = getMessagesThisRound().size();
        System.out.println(" numberOfMessagesSoFarThisRound "+ numberOfMessagesSoFarThisRound + "roundSize "+roundSize);
        if (numberOfMessagesSoFarThisRound == roundSize) {
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
        roundNumber++;
    }

    public int getRoundSize() {
        return roundSize;
    }
}
