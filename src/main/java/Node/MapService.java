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
public class MapService {
    private final MapController mapController;
    private final ThisNodeInfo thisNodeInfo;

    private Object maxNumberSynchronizer;
    private SnapshotInfo snapshotInfo;

    private MapInfo mapInfo;

    @Autowired
    public MapService(
            @Lazy MapController mapController,
            @Qualifier("Node/NodeConfigurator/thisNodeInfo") ThisNodeInfo thisNodeInfo,
            @Qualifier("Node/NodeConfigurator/maxNumberSynchronizer") Object maxNumberSynchronizer,
            @Qualifier("Node/NodeConfigurator/snapshotInfo") SnapshotInfo snapshotInfo
    ) {
        this.mapController = mapController;
        this.thisNodeInfo = thisNodeInfo;
        this.maxNumberSynchronizer = maxNumberSynchronizer;
        this.snapshotInfo = snapshotInfo;

        mapInfo = new MapInfo();
    }

    public void doActiveThings(){
        synchronized (maxNumberSynchronizer) {
            if(mapInfo.getMessagesSent() < thisNodeInfo.getMaxNumber()) {
                mapInfo.setActive(true);
                int numMessagesToSend = genNumMessagesToSend();
                //loop send message(s)
                for(int i = 0; i < numMessagesToSend; i++) {
                    sendToRandomNeighbor();
                    waitMinSendDelay();
                }
                //set passive
                mapInfo.setActive(false);
            }
            snapshotInfo.incrementProcessedMessages();
        }
    }

    public void sendToRandomNeighbor() {
        NodeInfo targetNeighbor = chooseRandomNeighbor();
        mapController.sendMapMessage(targetNeighbor.getUid());
        mapInfo.incrementMessagesSent();
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
        int maxNum = Math.min(thisNodeInfo.getMaxPerActive(), thisNodeInfo.getMaxNumber() - mapInfo.getMessagesSent());

        //the check is so that in case the number of messages that can be sent is > 0 but < maxPerActive
        if(maxNum < thisNodeInfo.getMinPerActive())
            return maxNum;

        Random rand = new Random();
        int numOfMessages = rand.nextInt((maxNum - thisNodeInfo.getMinPerActive() + 1)) + thisNodeInfo.getMinPerActive();
        return numOfMessages;
    }

    public NodeInfo chooseRandomNeighbor(){
        List<NodeInfo> neighbors = thisNodeInfo.getNeighbors();
        Random rand = new Random();
        NodeInfo chosenNeighbor = neighbors.get(rand.nextInt(neighbors.size()));
        return chosenNeighbor;
    }

//    public void mwoeIntraComponentSearch(int sourceUid) {
//        parentUid = sourceUid;
//        isSearched = true;
//        mapController.sendMwoeSearch(false);
//    }
}