package Node;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class MessageIntRoundSynchronizer<M extends RoundSynchronizable<Integer>> extends  MessageRoundSynchronizer<Integer, M> {
    public MessageIntRoundSynchronizer(int roundSize) {
        super(roundSize);
    }

    @Override
    public void enqueueMessage(M message) {
        int messageRoundId = message.getRoundId();
        ensureQueueForRoundIsInitialized(messageRoundId);
        roundMessages.get(messageRoundId).add(message);
    }

    public void incrementRoundNumber() {
        if(log.isTraceEnabled()) {
            log.trace("moving from round {} to round {}", getRoundId(), getRoundId() + 1);
        }
        int incrementedRoundNumber = getRoundId() + 1;
        setRoundId(incrementedRoundNumber);
        ensureQueueForRoundIsInitialized(incrementedRoundNumber);
    }

    public void ensureQueueForRoundIsInitialized(int roundNumber) {
        if(roundMessages.size() < roundNumber) {
            for(int i = roundMessages.size(); i <= roundNumber; i++) {
                roundMessages.put(i, new ConcurrentLinkedQueue<>());
            }
        }
    }
}
