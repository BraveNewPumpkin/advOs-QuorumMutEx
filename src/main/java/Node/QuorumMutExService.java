package Node;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class QuorumMutExService {
    private final QuorumMutExController quorumMutExController;
    private final ThisNodeInfo thisNodeInfo;
    private QuorumMutExInfo quorumMutExInfo;

    @Autowired
    public QuorumMutExService(
            @Lazy QuorumMutExController quorumMutExController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/MapConfig/mapInfo") QuorumMutExInfo quorumMutExInfo,
            @Qualifier("Node/NodeConfigurator/maxNumberSynchronizer") Object maxNumberSynchronizer,
    ) {
        this.quorumMutExController = quorumMutExController;
        this.thisNodeInfo = thisNodeInfo;
        this.quorumMutExInfo = quorumMutExInfo;
    }

    public void doActiveThings(){
        buildingTreeSynchronizer.enter();
        synchronized (maxNumberSynchronizer) {
            if(quorumMutExInfo.getMessagesSent() < thisNodeInfo.getMaxNumber()) {
                quorumMutExInfo.setActive(true);
                int numMessagesToSend = genNumMessagesToSend();
                //loop send message(s)
                for(int i = 0; i < numMessagesToSend; i++) {
                    sendToRandomNeighbor();
                    waitMinSendDelay();
                }
                //set passive
                quorumMutExInfo.setActive(false);
            }
            snapshotInfo.incrementProcessedMessages();
        }
    }

    public void sendToRandomNeighbor() {
        NodeInfo targetNeighbor = chooseRandomNeighbor();
        quorumMutExController.sendMapMessage(targetNeighbor.getUid());
        quorumMutExInfo.incrementMessagesSent();
    }

    public void waitMinSendDelay() {
        int minSendDelay = thisNodeInfo.getMinSendDelay();
        try {
            TimeUnit.MILLISECONDS.sleep(minSendDelay);
        } catch(java.lang.InterruptedException e) {
            //ignore
        }
    }

    public int genNumMessagesToSend() {
        //pick random number of messages to send between minPerActive and min(maxPerActive, maxNumber - messagesSent)
        int maxNum = Math.min(thisNodeInfo.getMaxPerActive(), thisNodeInfo.getMaxNumber() - quorumMutExInfo.getMessagesSent());

        //the check is so that in case the number of messages that can be sent is > 0 but < maxPerActive
        if(maxNum < thisNodeInfo.getMinPerActive())
            return maxNum;

        Random rand = new Random();
        int numOfMessages = rand.nextInt((maxNum - thisNodeInfo.getMinPerActive() + 1)) + thisNodeInfo.getMinPerActive();
        return numOfMessages;
    }

    public NodeInfo chooseRandomNeighbor(){
        List<NodeInfo> neighbors = thisNodeInfo.getQuorum();
        Random rand = new Random();
        NodeInfo chosenNeighbor = neighbors.get(rand.nextInt(neighbors.size()));
        return chosenNeighbor;
    }
}